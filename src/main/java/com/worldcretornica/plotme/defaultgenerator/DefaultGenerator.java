package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;

import com.worldcretornica.configuration.ConfigurationSection;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenerator;
import com.worldcretornica.plotme_core.bukkit.BukkitPlotMe_GeneratorManagerBridge;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import org.bukkit.generator.ChunkGenerator;

public class DefaultGenerator extends BukkitAbstractGenerator {

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new DefaultChunkGenerator(this, worldName);
    }

    @Override
    public void initialize(PlotMe_CorePlugin plotMeCorePlugin) {
        setupConfigs();
        for (String worldName : mainWorldsSection.getKeys(false)) {
            ConfigurationSection wgc = mainWorldsSection.getConfigurationSection(worldName.toLowerCase());
            plotMeCorePlugin.getAPI().addManager(worldName.toLowerCase(), new BukkitPlotMe_GeneratorManagerBridge(new DefaultPlotManager(this, wgc)));
        }
        setSchematicUtil(plotMeCorePlugin.getAPI().getSchematicUtil());

    }

    private void setupConfigs() {

        if (mainWorldsSection.getKeys(false).isEmpty()) {
            ConfigurationSection configurationSection = mainWorldsSection.createSection("plotworld");
            for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
                configurationSection.set(wcp.key(), wcp.value());
            }
        }
        saveConfigFile();
        for (String worldName : mainWorldsSection.getKeys(false)) {
            // Get config for world
            ConfigurationSection wgc = mainWorldsSection.getConfigurationSection(worldName.toLowerCase());
            // Set defaults for WorldGenConfig
            for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
                wgc.addDefault(wcp.key(), wcp.value());
            }
            saveConfigFile();
            // Validate config
            if (wgc.getInt(GROUND_LEVEL.key()) > 250 || wgc.getInt(GROUND_LEVEL.key()) <= 0) {
                getLogger().severe("RoadHeight above 250 is unsafe. This is the height at which your road is located. Setting it to 250.");
                wgc.set(GROUND_LEVEL.key(), 250);
            }
            super.putWGC(worldName.toLowerCase(), wgc);
        }
        saveConfigFile();
    }

}
