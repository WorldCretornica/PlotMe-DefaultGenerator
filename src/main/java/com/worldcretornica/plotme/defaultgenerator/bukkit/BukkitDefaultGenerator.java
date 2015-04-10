package com.worldcretornica.plotme.defaultgenerator.bukkit;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;

import com.worldcretornica.plotme.defaultgenerator.DefaultChunkGenerator;
import com.worldcretornica.plotme.defaultgenerator.DefaultPlotManager;
import com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenerator;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;

public class BukkitDefaultGenerator extends BukkitAbstractGenerator {

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new DefaultChunkGenerator(this, worldName.toLowerCase(), plotMePlugin);
    }

    @Override
    public void initialize(PlotMe_CorePlugin plotMeCorePlugin) {
        setupConfigs();
        assert mainWorldsSection != null;
        for (String worldName : mainWorldsSection.getKeys(false)) {
            ConfigurationSection wgc = mainWorldsSection.getConfigurationSection(worldName);
            plotMeCorePlugin.getAPI().addManager(worldName, new DefaultPlotManager(this, wgc));
        }
        setSchematicUtil(plotMeCorePlugin.getAPI().getSchematicUtil());

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

}
