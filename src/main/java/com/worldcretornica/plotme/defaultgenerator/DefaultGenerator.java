package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.AUCTION_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.BASE_BLOCK;
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
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.X_TRANSLATION;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.Z_TRANSLATION;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.flungo.bukkit.plotme.abstractgenerator.AbstractGenManager;
import me.flungo.bukkit.plotme.abstractgenerator.AbstractGenerator;
import me.flungo.bukkit.plotme.abstractgenerator.WorldGenConfig;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.mcstats.Metrics;

public class DefaultGenerator extends AbstractGenerator {

    public static final String CORE_OLD_CONFIG = "config-old.yml";
    public static final String DEFAULT_WORLD = "plotsworld";

    public String language;

    private Boolean advancedlogging;

    private DefaultPlotManager genPlotManager;

    @Override
    public void takedown() {
        genPlotManager = null;
        setAdvancedLogging(null);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldname, String id) {
        return new DefaultChunkGenerator(this, worldname);
    }

    public void importOldConfigs() {
        getLogger().info("Checking if there are any old PlotMe configs to import.");
        // Get the old config file
        final File oldConfigFile = new File(getCoreFolder(), CORE_OLD_CONFIG);

        // If it doesn't exist there is nothing to import
        if (!oldConfigFile.exists()) {
            getLogger().info("No old PlotMe configs to import.");
            return;
        }

        // Load the config from the file and get the worlds config section
        final FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);
        final ConfigurationSection oldWorldsCS = oldConfig.getConfigurationSection(WORLDS_CONFIG_SECTION);

        // If there are no worlds then there is nothing to import
        if (oldWorldsCS == null || oldWorldsCS.getKeys(false).isEmpty()) {
            getLogger().info("No old PlotMe configs to import.");
            return;
        }

        getLogger().info("Importing old PlotMe data");

        // Get the local worlds config section
        final ConfigurationSection worldsCS = getConfig().getConfigurationSection(WORLDS_CONFIG_SECTION);

        // Create a mapping from oldConfig to config
        final Map<String, String> mapping = new HashMap<String, String>();

        mapping.put("PlotSize", PLOT_SIZE.path);
        mapping.put("XTranslation", X_TRANSLATION.path);
        mapping.put("ZTranslation", Z_TRANSLATION.path);
        mapping.put("RoadHeight", GROUND_LEVEL.path);
        mapping.put("BottomBlockId", BASE_BLOCK.path);
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
                            getLogger().log(Level.WARNING,
                                    "Could not migrate {0}.{1} from {2} to {0}.{3} in {4}{5}: Path exists in desitnation. Please merge manually." + DEFAULT_CONFIG_NAME,
                                    new Object[]{fullPathBase, path, oldConfigFile, newPath, getConfigFolder(), File.separator});
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
            oldConfig.set(WORLDS_CONFIG_SECTION, null);
        }

        // Save the configs
        saveConfig();

        // If there is anything left then save, otherwise delete config-old.yml
        if (oldConfig.getKeys(false).isEmpty()) {
            oldConfigFile.delete();
            getLogger().info("Old data from PlotMe has been fully imported. " + CORE_OLD_CONFIG + " has been deleted.");
        } else {
            try {
                oldConfig.save(oldConfigFile);
                getLogger().info("Unimported config data in " + CORE_OLD_CONFIG + " of PlotMe, please review manually.");
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not save " + CORE_OLD_CONFIG + " to " + oldConfigFile, ex);
            }
        }
    }

    @Override
    public void initialize() {
        genPlotManager = new DefaultPlotManager(this);
        setupListeners();
        setupConfigs();
        setupMetrics();
    }

    private void setupListeners() {
        // Setup PluginListener
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PluginListener(this), this);
    }

    private void setupConfigs() {
        // Set defaults for WorldGenConfig
        for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
            WorldGenConfig.putDefault(wcp);
        }

        // Override defaults from AbstarctWorldConfigPath
        WorldGenConfig.putDefault(PLOT_SIZE, 32);

        // If no world are defined in our config, define a sample world for the user to be able to copy.
        if (!getConfig().contains(WORLDS_CONFIG_SECTION)) {
            // Get the config for an imaginary gridplots so that the config is generated.
            getWorldGenConfig(DEFAULT_WORLD);
            saveConfig();
        }

        ConfigurationSection worlds = getConfig().getConfigurationSection(WORLDS_CONFIG_SECTION);

        for (String worldname : worlds.getKeys(false)) {
            // Get config for world
            WorldGenConfig wgc = getWorldGenConfig(worldname);

            // Validate config
            if (wgc.getInt(GROUND_LEVEL) > 250) {
                getLogger().severe("RoadHeight above 250 is unsafe. This is the height at which your road is located. Setting it to 250.");
                wgc.set(GROUND_LEVEL, 250);
            }

            genPlotManager.putWGC(worldname, wgc);
        }

        saveConfig();
    }

    private void setupMetrics() {
        try {
            Metrics metrics = new Metrics(this);

            metrics.start();
        } catch (IOException ex) {
            Logger.getLogger(DefaultGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String addColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public Boolean getAdvancedLogging() {
        return advancedlogging;
    }

    private void setAdvancedLogging(Boolean advancedlogging) {
        this.advancedlogging = advancedlogging;
    }

    @Override
    public AbstractGenManager getGeneratorManager() {
        return genPlotManager;
    }
}
