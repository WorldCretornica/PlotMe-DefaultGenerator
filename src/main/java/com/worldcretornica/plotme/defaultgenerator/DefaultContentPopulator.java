package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FILL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_FLOOR_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_SIZE;

import com.worldcretornica.configuration.ConfigurationSection;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class DefaultContentPopulator extends BlockPopulator {

    private final ConfigurationSection worldGenConfig;

    public DefaultContentPopulator(ConfigurationSection worldGenConfig) {
        this.worldGenConfig = worldGenConfig;
    }

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        int plotSize = worldGenConfig.getInt(PLOT_SIZE.key());
        int pathSize = worldGenConfig.getInt(PATH_WIDTH.key());
        int roadHeight = worldGenConfig.getInt(GROUND_LEVEL.key());
        byte plotFloor = BukkitBlockRepresentation.getBlockData(worldGenConfig.getString(PLOT_FLOOR_BLOCK.key(), "2"));
        byte filling = BukkitBlockRepresentation.getBlockData(worldGenConfig.getString(FILL_BLOCK.key(), "3"));

        int xx = chunk.getX() << 4;
        int zz = chunk.getZ() << 4;

        double size = plotSize + pathSize;

        for (int x = xx; x < xx + 16; x++) {
            int valx = x;

            valx -= Math.ceil(((double) pathSize) / 2);
            valx = (valx % (int) size);
            if (valx < 0) {
                valx += size;
            }

            boolean modX = valx < plotSize;

            for (int z = zz; z < zz + 16; z++) {
                int valz = z;

                valz -= Math.ceil(((double) pathSize) / 2);
                valz = (valz % (int) size);
                if (valz < 0) {
                    valz += size;
                }

                boolean modZ = valz < plotSize;

                for (int y = 0; y <= roadHeight; y++) {
                    if (y < roadHeight) {
                        setData(world, x, y, z, filling);
                    } else if (modX && modZ) {
                        setData(world, x, y, z, plotFloor);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setData(World world, int x, int y, int z, byte val) {
        world.getBlockAt(x, y, z).setData(val, false);
    }
}
