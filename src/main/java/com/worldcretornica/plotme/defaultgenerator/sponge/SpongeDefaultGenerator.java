package com.worldcretornica.plotme.defaultgenerator.sponge;

import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

/**
 * Created by Matthew on 4/5/2015.
 */
@Plugin(id = "PlotMe-DefaultGenerator", name = "PlotMe-DefaultGenerator", version = "0.1-Sponge")
public class SpongeDefaultGenerator implements WorldGeneratorModifier {


    @Override
    public void modifyWorldGenerator(String worldName, DataContainer settings, WorldGenerator worldGenerator) {
        worldGenerator.setBiomeGenerator(new SpongeBiomeGenerator());
    }
}
