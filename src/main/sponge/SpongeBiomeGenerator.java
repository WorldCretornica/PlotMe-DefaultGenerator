package com.worldcretornica.plotme.defaultgenerator.sponge;

import org.spongepowered.api.util.gen.MutableBiomeArea;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.gen.BiomeGenerator;

/**
 * Created by Matthew on 4/5/2015.
 */
public class SpongeBiomeGenerator implements BiomeGenerator {

    @Override
    public void generateBiomes(MutableBiomeArea mutableBiomeArea) {
        mutableBiomeArea.fill(BiomeTypes.PLAINS);
    }
}
