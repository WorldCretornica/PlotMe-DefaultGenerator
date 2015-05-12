package com.worldcretornica.plotme.defaultgenerator.sponge;

import org.spongepowered.api.util.gen.BiomeBuffer;
import org.spongepowered.api.util.gen.MutableBlockBuffer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.GeneratorPopulator;

/**
 * Created by Matthew on 5/4/2015.
 */
public class PlotWorldBaseGenerator implements GeneratorPopulator {


    public PlotWorldBaseGenerator(long seed) {
    }

    @Override
    public void populate(World world, MutableBlockBuffer buffer, BiomeBuffer biomes) {
        buffer.setHorizontalLayer();
    }
}
