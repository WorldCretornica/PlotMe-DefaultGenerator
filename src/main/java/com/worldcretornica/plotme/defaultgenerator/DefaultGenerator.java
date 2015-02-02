package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.AUCTION_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FILL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FOR_SALE_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_FLOOR_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_SIZE;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PROTECTED_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_ALT_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_MAIN_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.WALL_BLOCK;

import com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath;
import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenerator;
import com.worldcretornica.plotme_core.bukkit.BukkitPlotMe_GeneratorManagerBridge;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DefaultGenerator extends BukkitAbstractGenerator {

    public static final String DEFAULT_WORLD = "plotworld";

    private DefaultPlotManager genPlotManager;

    @Override
    public void takedown() {    	
        genPlotManager = null;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new DefaultChunkGenerator(this, worldName);
    }

    /**
     * To be removed in 0.16
     */
    @Deprecated
    public void importOldConfigs() {
        // Get the old config file
        File coreConfigFile = new File(getCoreFolder(), CONFIG_NAME);

        // Load the config from the file and get the worlds config section
        FileConfiguration coreConfig = YamlConfiguration.loadConfiguration(coreConfigFile);
        ConfigurationSection oldWorldsCS = coreConfig.getConfigurationSection("worlds");

        // If there are no worlds then there is nothing to import
        if (oldWorldsCS == null || oldWorldsCS.getKeys(false).isEmpty()) {
            getLogger().info("No old PlotMe configs to import.");
            return;
        }

        // Get the local worlds config section
        ConfigurationSection worldsCS = getConfig().getConfigurationSection("worlds");

        if (worldsCS == null) {
            worldsCS = getConfig().createSection("worlds");
        }

        // Create a mapping from coreConfig to config
        Map<String, String> mapping = new HashMap<>();

        mapping.put("PlotSize", PLOT_SIZE.key);
        mapping.put("XTranslation", AbstractWorldConfigPath.X_TRANSLATION.path);
        mapping.put("ZTranslation", AbstractWorldConfigPath.Z_TRANSLATION.path);
        mapping.put("RoadHeight", GROUND_LEVEL.key);
        mapping.put("PlotFillingBlockId", FILL_BLOCK.key);
        mapping.put("PathWidth", PATH_WIDTH.key);
        mapping.put("PlotFloorBlockId", PLOT_FLOOR_BLOCK.key);
        mapping.put("RoadMainBlockId", ROAD_MAIN_BLOCK.key);
        mapping.put("RoadStripeBlockId", ROAD_ALT_BLOCK.key);
        mapping.put("WallBlockId", WALL_BLOCK.key);
        mapping.put("ProtectedWallBlockId", PROTECTED_WALL_BLOCK.key);
        mapping.put("AuctionWallBlockId", AUCTION_WALL_BLOCK.key);
        mapping.put("ForSaleWallBlockId", FOR_SALE_WALL_BLOCK.key);

        // Import each world
        for (String worldName : oldWorldsCS.getKeys(false)) {
            getLogger().log(Level.INFO, "Importing world {0} from PlotMe", worldName);
            ConfigurationSection oldWorldCS = oldWorldsCS.getConfigurationSection(worldName);

            // Get the local config world section and create it if it doesn't exist
            ConfigurationSection worldCS = worldsCS.getConfigurationSection(worldName);
            if (worldCS == null) {
                worldCS = worldsCS.createSection(worldName);
            }

            // For each key import config and rename where required.
            for (String path : oldWorldCS.getKeys(true)) {
                if (mapping.containsKey(path)) {
                    String newPath = mapping.get(path);
                    if (worldCS.contains(newPath)) {
                        if (worldCS.get(newPath).equals(oldWorldCS.get(path))) {
                            // Great no work to do except deleting from the old config
                            oldWorldCS.set(path, null);
                        } else {
                            // Can't migrate the key
                            String fullPathBase = oldWorldCS.getCurrentPath();
                            getLogger().log(Level.WARNING,
                                            "Could not migrate {0}.{1} from {2} to {0}.{3} in {4}{5}: Path exists in destination. Please merge manually."
                                            + CONFIG_NAME,
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
                oldWorldsCS.set(worldName, null);
            }

            // Add world to Manager
            genPlotManager.putWGC(worldName, getWorldGenConfig(worldName));
        }

        // If all worlds are imported, delete worlds CS from config-old.yml
        if (oldWorldsCS.getKeys(false).isEmpty()) {
            coreConfig.set("worlds", null);
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
        
        PluginManager pm = Bukkit.getPluginManager();
        PlotMe_CorePlugin plotMe = (PlotMe_CorePlugin) pm.getPlugin("PlotMe");
        if (plotMe != null) {
            ConfigurationSection worlds = getConfig().getConfigurationSection("worlds");

        	try {
                for (String worldName : worlds.getKeys(false)) {
                    plotMe.getAPI().addManager(worldName.toLowerCase(), new BukkitPlotMe_GeneratorManagerBridge(getGeneratorManager()));
                }
        	} catch(Exception e) {
        		getLogger().severe("Unable to hook to PlotMe Core");
        		e.printStackTrace();
        	}
        }
    }

    private void setupConfigs() {
        // Set defaults for WorldGenConfig
        for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
            WorldGenConfig.putDefault(wcp);
        }

        // If no world are defined in our config, define a sample world for the user to be able to copy.
        if (!getConfig().contains("worlds")) {
            // Get the config for an imaginary grid plots so that the config is generated.
            getWorldGenConfig(DEFAULT_WORLD);
            saveConfig();
        }

        ConfigurationSection worlds = getConfig().getConfigurationSection("worlds");

        for (String worldName : worlds.getKeys(false)) {
            // Get config for world
            WorldGenConfig wgc = getWorldGenConfig(worldName.toLowerCase());

            // Validate config
            if (wgc.getInt(GROUND_LEVEL) > 250 || wgc.getInt(GROUND_LEVEL) <= 0) {
                getLogger().severe("RoadHeight above 250 is unsafe. This is the height at which your road is located. Setting it to 250.");
                wgc.set(GROUND_LEVEL, 250);
            }
            wgc.set("UnclaimedBorder", "44:7");
            genPlotManager.putWGC(worldName.toLowerCase(), wgc);
        }
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("plotGenVersion".equalsIgnoreCase(command.getName())) {
            sender.sendMessage("PlotMe Generator Version: 0.15.2 (010115)");
            return true;
        }
        return false;
    }

    private void setupMetrics() {
        try {
            Metrics metrics = new Metrics(this);

            metrics.start();
        } catch (IOException ignored) {
        }
    }

    @Override
    public BukkitAbstractGenManager getGeneratorManager() {
        return genPlotManager;
    }
}
