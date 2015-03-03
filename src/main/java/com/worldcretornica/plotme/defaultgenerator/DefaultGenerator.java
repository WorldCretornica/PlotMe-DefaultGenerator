package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;

import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenerator;
import com.worldcretornica.plotme_core.bukkit.BukkitPlotMe_GeneratorManagerBridge;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.mcstats.Metrics;

import java.io.IOException;

public class DefaultGenerator extends BukkitAbstractGenerator {

    private DefaultPlotManager genPlotManager;

    @Override
    public void takedown() {    	
        genPlotManager = null;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new DefaultChunkGenerator(this, worldName);
    }

    @Override
    public void initialize() {
        genPlotManager = new DefaultPlotManager(this);
        setupConfigs();
        setupMetrics();

        PluginManager pm = Bukkit.getPluginManager();
        PlotMe_CorePlugin plotMe = (PlotMe_CorePlugin) pm.getPlugin("PlotMe");
        if (plotMe != null) {

            for (String worldName : mainWorldsSection.getKeys(false)) {
                plotMe.getAPI().addManager(worldName.toLowerCase(), new BukkitPlotMe_GeneratorManagerBridge(getGeneratorManager()));
            }
            setSchematicUtil(plotMe.getAPI().getSchematicUtil());
        }
    }

    private void setupConfigs() {
        // Set defaults for WorldGenConfig
        for (DefaultWorldConfigPath wcp : DefaultWorldConfigPath.values()) {
            WorldGenConfig.putDefault(wcp);
        }

        for (String worldName : mainWorldsSection.getKeys(false)) {
            // Get config for world
            WorldGenConfig wgc = getWorldGenConfig(worldName.toLowerCase());

            // Validate config
            if (wgc.getInt(GROUND_LEVEL) > 250 || wgc.getInt(GROUND_LEVEL) <= 0) {
                getLogger().severe("RoadHeight above 250 is unsafe. This is the height at which your road is located. Setting it to 250.");
                wgc.set(GROUND_LEVEL, 250);
            }
            genPlotManager.putWGC(worldName.toLowerCase(), wgc);
        }
        saveConfigFile();
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
