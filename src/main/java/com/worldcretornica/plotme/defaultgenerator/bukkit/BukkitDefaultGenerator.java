package com.worldcretornica.plotme.defaultgenerator.bukkit;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;

import com.worldcretornica.plotme.defaultgenerator.DefaultChunkGenerator;
import com.worldcretornica.plotme.defaultgenerator.DefaultPlotManager;
import com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenerator;
import com.worldcretornica.plotme_core.bukkit.api.BukkitWorld;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.mcstats.Metrics;

import java.io.IOException;

public class BukkitDefaultGenerator extends BukkitAbstractGenerator {

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new DefaultChunkGenerator(this, worldName.toLowerCase());
    }

    @Override
    public void initialize() {
        setupConfigs();
        setupMetrics();
    }

    @Override
    protected void worldLoadEvent(World world) {
        if (world.getGenerator() instanceof DefaultChunkGenerator) {
            getLogger().info(world.getName() + " was found using the generator.");
            getLogger().info("Sending data to PlotMe-Core");
            String worldName = world.getName().toLowerCase();
            getLogger().info("Looking for " + worldName + " in the config file.");
            assert mainWorldsSection != null;
            ConfigurationSection wgc = mainWorldsSection.getConfigurationSection(worldName);
            if (wgc == null) {
                getLogger().severe(worldName + " was not found in the config file!");
                getLogger().severe("Did you remember to modify your configuration file?");
                getLogger().severe("Please review the setup instructions at http://plotme.worldcretornica.com");
            }
            BukkitWorld world1 = new BukkitWorld(world);
            plotMePlugin.getAPI().addManager(world1, new DefaultPlotManager(this, wgc, world1));
        }
    }

    private void setupConfigs() {
        assert mainWorldsSection != null;
        if (mainWorldsSection.getKeys(false).isEmpty()) {
            ConfigurationSection configurationSection = mainWorldsSection.createSection("plotworld");
            for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
                configurationSection.set(wcp.key(), wcp.value());
            }
        }
        saveConfigFile();
        for (String worldName : mainWorldsSection.getKeys(false)) {
            ConfigurationSection wgc;
            wgc = createConfigSection(worldName);
            // Get config for world
            // Set defaults for WorldGenConfig
            for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
                wgc.addDefault(wcp.key(), wcp.value());
            }

            saveConfigFile();
            // Validate config
            if (wgc.getInt(GROUND_LEVEL.key()) > 250 || wgc.getInt(GROUND_LEVEL.key()) < 1) {
                getLogger().severe("Unsafe RoadHeight. Resetting to 64");
                wgc.set(GROUND_LEVEL.key(), 64);
            }
            super.putWGC(worldName.toLowerCase(), wgc);
        }
        saveConfigFile();
    }

    public ConfigurationSection createConfigSection(String worldName) {
        String wLower = worldName.toLowerCase();
        ConfigurationSection wgc;
        assert mainWorldsSection != null;
        if (mainWorldsSection.contains(wLower)) {
            wgc = mainWorldsSection.getConfigurationSection(wLower);
        } else {
            wgc = mainWorldsSection.createSection(wLower);
            for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
                wgc.set(wcp.key(), wcp.value());
            }
        }
        saveConfigFile();
        return wgc;
    }

    private void setupMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException ignored) {
        }
    }
}
