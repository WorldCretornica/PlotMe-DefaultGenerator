package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_SIZE;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_ALT_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_MAIN_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.UNCLAIMED_WALL;

import com.worldcretornica.configuration.ConfigurationSection;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class DefaultRoadPopulator extends BlockPopulator {

    private final ConfigurationSection wgc;

    public DefaultRoadPopulator(ConfigurationSection wgc) {
        this.wgc = wgc;
    }

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        int plotSize = wgc.getInt(PLOT_SIZE.key());
        int pathSize = wgc.getInt(PATH_WIDTH.key());
        int roadHeight = wgc.getInt(GROUND_LEVEL.key());
        byte wall = BukkitBlockRepresentation.getBlockData(wgc.getString(UNCLAIMED_WALL.key(), "44:7"));
        byte floorMain = BukkitBlockRepresentation.getBlockData(wgc.getString(ROAD_MAIN_BLOCK.key(), "5"));
        byte floorAlt = BukkitBlockRepresentation.getBlockData(wgc.getString(ROAD_ALT_BLOCK.key(), "5:2"));

        int xx = chunk.getX() << 4;
        int zz = chunk.getZ() << 4;

        double size = plotSize + pathSize;

        double n1;
        double n2;
        double n3;
        int mod2 = 0;
        int mod1 = 1;

        if (pathSize % 2 == 1) {
            n1 = Math.ceil((double) pathSize / 2) - 2;
            n2 = Math.ceil((double) pathSize / 2) - 1;
            n3 = Math.ceil((double) pathSize / 2);
            mod2 = -1;
        } else {
            n1 = Math.floor((double) pathSize / 2) - 2;
            n2 = Math.floor((double) pathSize / 2) - 1;
            n3 = Math.floor((double) pathSize / 2);
        }

        for (int x = xx; x < xx + 16; x++) {
            for (int z = zz; z < zz + 16; z++) {

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
                        setData(world, x, roadHeight, z, floorMain);
                    } else {
                        setData(world, x, roadHeight, z, floorMain);
                        setData(world, x, roadHeight + 1, z, wall);
                    }
                } else {
                    boolean found5 = false;
                    for (double i = n2; i >= 0; i--) {
                        if ((x - i + mod1) % size == 0 || (x + i + mod2) % size == 0) {
                            found5 = true;
                            break;
                        }
                    }

                    if (!found5 && ((z - n3 + mod1) % size == 0 || (z + n3 + mod2) % size == 0)) {
                        setData(world, x, roadHeight, z, floorMain);
                        setData(world, x, roadHeight + 1, z, wall);
                    }

                    if ((x - n2 + mod1) % size == 0 || (x + n2 + mod2) % size == 0) // middle+2
                    {
                        if ((z - n3 + mod1) % size == 0 || (z + n3 + mod2) % size == 0 || (z - n2 + mod1) % size == 0
                                || (z + n2 + mod2) % size == 0) {
                            setData(world, x, roadHeight, z, floorMain);
                        } else {
                            setData(world, x, roadHeight, z, floorAlt);
                        }
                    } else if ((x - n1 + mod1) % size == 0 || (x + n1 + mod2) % size == 0) // middle+2
                    {
                        if ((z - n2 + mod1) % size == 0 || (z + n2 + mod2) % size == 0 || (z - n1 + mod1) % size == 0
                                || (z + n1 + mod2) % size == 0) {
                            setData(world, x, roadHeight, z, floorAlt);
                        } else {
                            setData(world, x, roadHeight, z, floorMain);
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
                            setData(world, x, roadHeight, z, floorMain);
                        } else if ((z - n2 + mod1) % size == 0 || (z + n2 + mod2) % size == 0) {
                            setData(world, x, roadHeight, z, floorAlt);
                        } else {
                            boolean found2 = false;
                            for (double i = n1; i >= 0; i--) {
                                if ((z - i + mod1) % size == 0 || (z + i + mod2) % size == 0) {
                                    found2 = true;
                                    break;
                                }
                            }

                            if (found2) {
                                setData(world, x, roadHeight, z, floorMain);
                            } else {
                                boolean found3 = false;
                                for (double i = n3; i >= 0; i--) {
                                    if ((x - i + mod1) % size == 0 || (x + i + mod2) % size == 0) {
                                        found3 = true;
                                        break;
                                    }
                                }

                                if (found3) {
                                    setData(world, x, roadHeight, z, floorMain);
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
