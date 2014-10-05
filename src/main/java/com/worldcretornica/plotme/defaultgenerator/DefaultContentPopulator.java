package com.worldcretornica.plotme.defaultgenerator;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;

import java.util.Random;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.*;

public class DefaultContentPopulator extends BlockPopulator {

    private final DefaultGenerator plugin;
    private final String worldname;

    public DefaultContentPopulator(DefaultGenerator plugin, String worldname) {
        this.plugin = plugin;
        this.worldname = worldname;
    }

    @Override
    public void populate(World w, Random rand, Chunk chunk) {
        WorldGenConfig wgc = plugin.getGeneratorManager().getWGC(worldname);

        final int plotsize = wgc.getInt(PLOT_SIZE);
        final int pathsize = wgc.getInt(PATH_WIDTH);
        final int roadheight = wgc.getInt(GROUND_LEVEL);
        final byte plotfloor = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getData();
        final byte filling = wgc.getBlockRepresentation(FILL_BLOCK).getData();

        final int xx = chunk.getX() << 4;
        final int zz = chunk.getZ() << 4;

        double size = plotsize + pathsize;

        for (int x = xx; x < xx + 16; x++) {
            int valx = x;

            valx -= Math.ceil(((double) pathsize) / 2);
            valx = (valx % (int) size);
            if (valx < 0) valx += size;

            boolean modX = valx < plotsize;

            for (int z = zz; z < zz + 16; z++) {
                int valz = z;

                valz -= Math.ceil(((double) pathsize) / 2);
                valz = (valz % (int) size);
                if (valz < 0) valz += size;

                boolean modZ = valz < plotsize;

                for (int y = 0; y <= roadheight; y++) {
                    if (y < roadheight) {
                        setData(w, x, y, z, filling);
                    } else if (modX && modZ) {
                        setData(w, x, y, z, plotfloor);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setData(World w, int x, int y, int z, byte val) {
        w.getBlockAt(x, y, z).setData(val, false);
    }
}
