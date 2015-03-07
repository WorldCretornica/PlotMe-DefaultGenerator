package com.worldcretornica.plotme.defaultgenerator.test;

import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_core.PlotId;
import org.junit.Assert;
import org.junit.Test;

public class GeneratorTest {

    @Test
    public void testGetPlotID() {

        for (int road = 3; road < 10; road++) {
            for (int size = 1; size < 30; size++) {
                for (int x = -50; x < 50; x++) {
                    for (int z = -50; z < 50; z++) {
                        Assert.assertEquals(oldPlotId(road, size, x, z), BukkitAbstractGenManager.internalgetPlotId(road, size, x, z));
                    }
                }
            }
        }
    }

    private PlotId oldPlotId(int pathsize, int size, int posx, int posz) {

        double n3;
        int mod2 = 0;

        int x = (int) Math.ceil((double) posx / size);
        int z = (int) Math.ceil((double) posz / size);

        if (pathsize % 2 == 1) {
            n3 = Math.ceil(((double) pathsize) / 2);
            mod2 = -1;
        } else {
            n3 = Math.floor(((double) pathsize) / 2);
        }

        boolean road = false;
        int mod1 = 1;
        for (double i = n3; i >= 0; i--) {
            if ((posx - i + mod1) % size == 0 || (posx + i + mod2) % size == 0) {
                road = true;
                x = (int) Math.ceil((posx - n3) / size);
            }
            if ((posz - i + mod1) % size == 0 || (posz + i + mod2) % size == 0) {
                road = true;
                z = (int) Math.ceil((posz - n3) / size);
            }
        }

        if (road) {
            return null;
        } else {
            return new PlotId(x, z);
        }
    }
}
