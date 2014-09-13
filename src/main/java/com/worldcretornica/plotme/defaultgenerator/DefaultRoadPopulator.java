package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_SIZE;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_ALT_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_MAIN_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.WALL_BLOCK;

import java.util.Random;

import me.flungo.bukkit.plotme.abstractgenerator.WorldGenConfig;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

public class DefaultRoadPopulator extends BlockPopulator {

    private final DefaultGenerator plugin;
    private final String worldname;

    public DefaultRoadPopulator(DefaultGenerator plugin, String worldname) {
        this.plugin = plugin;
        this.worldname = worldname;
    }

    @Override
    public void populate(World w, Random rand, Chunk chunk) {
        WorldGenConfig wgc = plugin.getGeneratorManager().getWGC(worldname);

        final int plotsize = wgc.getInt(PLOT_SIZE);
        final int pathsize = wgc.getInt(PATH_WIDTH);
        final int roadheight = wgc.getInt(GROUND_LEVEL);
        final byte wall = wgc.getBlockRepresentation(WALL_BLOCK).getData();
        final byte floorMain = wgc.getBlockRepresentation(ROAD_MAIN_BLOCK).getData();
        final byte floorAlt = wgc.getBlockRepresentation(ROAD_ALT_BLOCK).getData();

        final int xx = chunk.getX() << 4;
        final int zz = chunk.getZ() << 4;

        double size = plotsize + pathsize;

        double n1;
        double n2;
        double n3;
        int mod2 = 0;
        int mod1 = 1;

        if (pathsize % 2 == 1) {
            n1 = Math.ceil(((double) pathsize) / 2) - 2;
            n2 = Math.ceil(((double) pathsize) / 2) - 1;
            n3 = Math.ceil(((double) pathsize) / 2);
        } else {
            n1 = Math.floor(((double) pathsize) / 2) - 2;
            n2 = Math.floor(((double) pathsize) / 2) - 1;
            n3 = Math.floor(((double) pathsize) / 2);
        }

        if (pathsize % 2 == 1) {
            mod2 = -1;
        }

        for (int x = xx; x < xx + 16; x++) {
            for (int z = zz; z < zz + 16; z++) {
                
                int y = roadheight;

                if ((x - n3 + mod1) % size == 0 || (x + n3 + mod2) % size == 0) // middle+3
                {
                    boolean found = false;
                    for (double i = n2; i >= 0; i--) {
                        if ((z - i + mod1) % size == 0 || (z + i + mod2) % size == 0) {
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        setData(w, x, y, z, floorMain);
                    } else {
                        setData(w, x, y, z, floorMain);
                        setData(w, x, y + 1, z, wall);
                    }
                } else {
                    boolean found5 = false;
                    for (double i = n2; i >= 0; i--) {
                        if ((x - i + mod1) % size == 0 || (x + i + mod2) % size == 0) {
                            found5 = true;
                            break;
                        }
                    }

                    if (!found5) {
                        if ((z - n3 + mod1) % size == 0 || (z + n3 + mod2) % size == 0) {
                            setData(w, x, y, z, floorMain);
                            setData(w, x, y + 1, z, wall);
                        }
                    }

                    if ((x - n2 + mod1) % size == 0 || (x + n2 + mod2) % size == 0) // middle+2
                    {
                        if ((z - n3 + mod1) % size == 0 || (z + n3 + mod2) % size == 0 || (z - n2 + mod1) % size == 0 || (z + n2 + mod2) % size == 0) {
                            setData(w, x, y, z, floorMain);
                        } else {
                            setData(w, x, y, z, floorAlt);
                        }
                    } else if ((x - n1 + mod1) % size == 0 || (x + n1 + mod2) % size == 0) // middle+2
                    {
                        if ((z - n2 + mod1) % size == 0 || (z + n2 + mod2) % size == 0 || (z - n1 + mod1) % size == 0 || (z + n1 + mod2) % size == 0) {
                            setData(w, x, y, z, floorAlt);
                        } else {
                            setData(w, x, y, z, floorMain);
                        }
                    } else {
                        boolean found = false;
                        for (double i = n1; i >= 0; i--) {
                            if ((z - i + mod1) % size == 0 || (z + i + mod2) % size == 0) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            setData(w, x, y, z, floorMain);
                        } else {
                            if ((z - n2 + mod1) % size == 0 || (z + n2 + mod2) % size == 0) {
                                setData(w, x, y, z, floorAlt);
                            } else {
                                boolean found2 = false;
                                for (double i = n1; i >= 0; i--) {
                                    if ((z - i + mod1) % size == 0 || (z + i + mod2) % size == 0) {
                                        found2 = true;
                                        break;
                                    }
                                }

                                if (found2) {
                                    setData(w, x, y, z, floorMain);
                                } else {
                                    boolean found3 = false;
                                    for (double i = n3; i >= 0; i--) {
                                        if ((x - i + mod1) % size == 0 || (x + i + mod2) % size == 0) {
                                            found3 = true;
                                            break;
                                        }
                                    }

                                    if (found3) {
                                        setData(w, x, y, z, floorMain);
                                    }
                                }
                            }
                        }
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
