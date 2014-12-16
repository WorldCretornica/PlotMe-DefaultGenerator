package com.worldcretornica.plotme.defaultgenerator;

import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

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

        int plotsize = wgc.getInt(PLOT_SIZE);
        int pathsize = wgc.getInt(PATH_WIDTH);
        int roadheight = wgc.getInt(GROUND_LEVEL);
        byte plotfloor = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getData();
        byte filling = wgc.getBlockRepresentation(FILL_BLOCK).getData();

        int xx = chunk.getX() << 4;
        int zz = chunk.getZ() << 4;

        int size = plotsize + pathsize;

        for (int x = xx; x < xx + 16; x++) {
            int valx = x;

            valx -= Math.ceil(pathsize / 2);
            valx %= size;
            if (valx < 0) valx += size;

            boolean modX = valx < plotsize;

            for (int z = zz; z < zz + 16; z++) {
                int valz = z;

                valz -= Math.ceil(pathsize / 2);
                valz %= size;
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
    private void setData(World world, int x, int y, int z, byte val) {
        world.getBlockAt(x, y, z).setData(val, false);
    }
}
