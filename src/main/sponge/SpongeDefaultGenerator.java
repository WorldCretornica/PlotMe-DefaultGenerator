package com.worldcretornica.plotme.defaultgenerator.sponge;

import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

/**
 * Created by Matthew on 4/5/2015.
 */
@Plugin(id = "PlotMe-DefaultGenerator", name = "PlotMe-DefaultGenerator", version = "0.1-Sponge")
public class SpongeDefaultGenerator implements WorldGeneratorModifier {

    @Inject
    private Game game;

    @Subscribe
    public void onInitialize(InitializationEvent event) {
        game.getRegistry().registerWorldGeneratorModifier(this);
    }

    @Override
    public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator worldGenerator) {
        worldGenerator.setBaseGeneratorPopulator(new PlotWorldBaseGenerator(world.getSeed()));
    }

    @Override
    public String getId() {
        return "PlotMeDefaultGenerator";
    }

    @Override
    public String getName() {
        return "PlotMe-DefaultGenerator";
    }
}
