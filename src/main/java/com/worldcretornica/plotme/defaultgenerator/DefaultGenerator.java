package com.worldcretornica.plotme.defaultgenerator;

import com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath;
import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenerator;
import com.worldcretornica.plotme_core.PlotMe_Core;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.*;

public class DefaultGenerator extends BukkitAbstractGenerator {

    public static final String DEFAULT_WORLD = "plotworld";

    private DefaultPlotManager genPlotManager;

    @Override
    public void takedown() {
        genPlotManager = null;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldname, String id) {
        return new DefaultChunkGenerator(this, worldname);
    }

    public void importOldConfigs() {
        // Get the old config file
        File coreConfigFile = new File(getCoreFolder(), CONFIG_NAME);

        // Load the config from the file and get the worlds config section
        FileConfiguration coreConfig = YamlConfiguration.loadConfiguration(coreConfigFile);
        ConfigurationSection oldWorldsCS = coreConfig.getConfigurationSection(PlotMe_Core.WORLDS_CONFIG_SECTION);

        // If there are no worlds then there is nothing to import
        if (oldWorldsCS == null || oldWorldsCS.getKeys(false).isEmpty()) {
            getLogger().info("No old PlotMe configs to import.");
            return;
        }

        // Get the local worlds config section
        ConfigurationSection worldsCS = getConfig().getConfigurationSection(PlotMe_Core.WORLDS_CONFIG_SECTION);

        if (worldsCS == null) {
            worldsCS = getConfig().createSection(PlotMe_Core.WORLDS_CONFIG_SECTION);
        }

        // Create a mapping from coreConfig to config
        Map<String, String> mapping = new HashMap<>();

        mapping.put("PlotSize", PLOT_SIZE.path);
        mapping.put("XTranslation", AbstractWorldConfigPath.X_TRANSLATION.path);
        mapping.put("ZTranslation", AbstractWorldConfigPath.Z_TRANSLATION.path);
        mapping.put("RoadHeight", GROUND_LEVEL.path);
        mapping.put("PlotFillingBlockId", FILL_BLOCK.path);
        mapping.put("PathWidth", PATH_WIDTH.path);
        mapping.put("PlotFloorBlockId", PLOT_FLOOR_BLOCK.path);
        mapping.put("RoadMainBlockId", ROAD_MAIN_BLOCK.path);
        mapping.put("RoadStripeBlockId", ROAD_ALT_BLOCK.path);
        mapping.put("WallBlockId", WALL_BLOCK.path);
        mapping.put("ProtectedWallBlockId", PROTECTED_WALL_BLOCK.path);
        mapping.put("AuctionWallBlockId", AUCTION_WALL_BLOCK.path);
        mapping.put("ForSaleWallBlockId", FOR_SALE_WALL_BLOCK.path);

        // Import each world
        for (String worldname : oldWorldsCS.getKeys(false)) {
            getLogger().log(Level.INFO, "Importing world {0} from PlotMe", worldname);
            ConfigurationSection oldWorldCS = oldWorldsCS.getConfigurationSection(worldname);

            // Get the local config world section and create it if it doesn't exist
            ConfigurationSection worldCS = worldsCS.getConfigurationSection(worldname);
            if (worldCS == null) {
                worldCS = worldsCS.createSection(worldname);
            }

            // For each path import config and rename where required.
            for (String path : oldWorldCS.getKeys(true)) {
                if (mapping.containsKey(path)) {
                    String newPath = mapping.get(path);
                    if (worldCS.contains(newPath)) {
                        if (worldCS.get(newPath).equals(oldWorldCS.get(path))) {
                            // Great no work to do except deleting from the old config
                            oldWorldCS.set(path, null);
                        } else {
                            // Can't migrate the path
                            String fullPathBase = oldWorldCS.getCurrentPath();
                            getLogger().log(Level.WARNING, "Could not migrate {0}.{1} from {2} to {0}.{3} in {4}{5}: Path exists in desitnation. Please merge manually." + CONFIG_NAME,
                                                   new Object[]{fullPathBase, path, coreConfigFile, newPath, getConfigFolder(), File.separator});
                        }
                    } else {
                        // Migrate!
                        worldCS.set(newPath, oldWorldCS.get(path));
                        oldWorldCS.set(path, null);
                    }
                }
            }

            // If full imported delete from config-old.yml
            if (oldWorldCS.getKeys(false).isEmpty()) {
                oldWorldsCS.set(worldname, null);
            }

            // Add world to Manager
            genPlotManager.putWGC(worldname, getWorldGenConfig(worldname));
        }

        // If all worlds are imported, delete worlds CS from config-old.yml
        if (oldWorldsCS.getKeys(false).isEmpty()) {
            coreConfig.set(PlotMe_Core.WORLDS_CONFIG_SECTION, null);
        }

        // Save the configs
        saveConfig();

        // If there is anything left then save, otherwise delete config-old.yml
        try {
            coreConfig.save(coreConfigFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + CONFIG_NAME + " to " + coreConfigFile, ex);
        }
    }

    @Override
    public void initialize() {
        genPlotManager = new DefaultPlotManager(this);
        setupConfigs();
        setupMetrics();
    }

    private void setupConfigs() {
        // Import old configs
        importOldConfigs();

        // Set defaults for WorldGenConfig
        for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
            WorldGenConfig.putDefault(wcp);
        }

        // If no world are defined in our config, define a sample world for the user to be able to copy.
        if (!getConfig().contains(PlotMe_Core.WORLDS_CONFIG_SECTION)) {
            // Get the config for an imaginary gridplots so that the config is generated.
            getWorldGenConfig(DEFAULT_WORLD);
            saveConfig();
        }

        ConfigurationSection worlds = getConfig().getConfigurationSection(PlotMe_Core.WORLDS_CONFIG_SECTION);

        for (String worldname : worlds.getKeys(false)) {
            // Get config for world
            WorldGenConfig wgc = getWorldGenConfig(worldname);

            // Validate config
            if (wgc.getInt(GROUND_LEVEL) > 250 || wgc.getInt(GROUND_LEVEL) <= 0) {
                getLogger().severe("RoadHeight above 250 is unsafe. This is the height at which your road is located. Setting it to 250.");
                wgc.set(GROUND_LEVEL, 250);
            }

            genPlotManager.putWGC(worldname, wgc);
        }

        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("plotGenVersion".equalsIgnoreCase(command.getName())) {
            sender.sendMessage("PlotMe Generator Version: 0.15.1 (122514)");
            return true;
        }
        return false;
    }

    private void setupMetrics() {
        try {
            Metrics metrics = new Metrics(this);

            metrics.start();
        } catch (IOException ex) {
        }
    }

    @Override
    public BukkitAbstractGenManager getGeneratorManager() {
        return genPlotManager;
    }
}
