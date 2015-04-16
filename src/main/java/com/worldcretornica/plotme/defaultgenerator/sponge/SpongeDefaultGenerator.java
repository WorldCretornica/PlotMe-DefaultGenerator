package com.worldcretornica.plotme.defaultgenerator.sponge;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

/**
 * Created by Matthew on 4/5/2015.
 */
@Plugin(id = "PlotMe-DefaultGenerator", name = "PlotMe-DefaultGenerator", version = "0.1-Sponge")
public class SpongeDefaultGenerator implements WorldGeneratorModifier {


    @Override
    public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator worldGenerator) {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
