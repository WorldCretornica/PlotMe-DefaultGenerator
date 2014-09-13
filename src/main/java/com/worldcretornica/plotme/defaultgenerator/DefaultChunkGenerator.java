package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.BASE_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FILL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_FLOOR_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_SIZE;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_ALT_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_MAIN_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.WALL_BLOCK;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.flungo.bukkit.plotme.abstractgenerator.AbstractChunkGenerator;
import me.flungo.bukkit.plotme.abstractgenerator.WorldGenConfig;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

public class DefaultChunkGenerator extends AbstractChunkGenerator {

    private final String worldname;
    private final DefaultGenerator plugin;
    private final List<BlockPopulator> blockPopulators = new ArrayList<BlockPopulator>();

    public DefaultChunkGenerator(DefaultGenerator instance, String worldname) {
        super(instance, worldname);
        this.plugin = instance;
        this.worldname = worldname;
        blockPopulators.add(new DefaultRoadPopulator(plugin, worldname));
        blockPopulators.add(new DefaultContentPopulator(plugin, worldname));
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return blockPopulators;
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes) {
        WorldGenConfig wgc = plugin.getGeneratorManager().getWGC(worldname);
        final int maxY = world.getMaxHeight();

        final int plotsize = wgc.getInt(PLOT_SIZE);
        final int pathsize = wgc.getInt(PATH_WIDTH);
        final int roadheight = wgc.getInt(GROUND_LEVEL);
        final short bottom = wgc.getBlockRepresentation(BASE_BLOCK).getId();
        final short wall = wgc.getBlockRepresentation(WALL_BLOCK).getId();
        final short floorMain = wgc.getBlockRepresentation(ROAD_MAIN_BLOCK).getId();
        final short floorAlt = wgc.getBlockRepresentation(ROAD_ALT_BLOCK).getId();
        final short plotfloor = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getId();
        final short filling = wgc.getBlockRepresentation(FILL_BLOCK).getId();

        short[][] result = new short[maxY / 16][];

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

        for (int x = 0; x < 16; x++) {
        	int valx = ((cx << 4) + x);

            for (int z = 0; z < 16; z++) {
                int height = roadheight + 2;
                int valz = ((cz << 4) + z);

                biomes.setBiome(x, z, Biome.PLAINS);

                for (int y = 0; y < height; y++) {
                    if (y == 0) {
                        setBlock(result, x, y, z, bottom);

                    } else if (y == roadheight) {
                        if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
                        {
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                setBlock(result, x, y, z, floorAlt);
                            } else {
                                setBlock(result, x, y, z, filling);
                            }
                        } else if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
                        {
                            if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
                                    || (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorAlt);
                            } else {
                                setBlock(result, x, y, z, floorMain);
                            }
                        } else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
                        {
                            if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0
                                    || (valz - n1 + mod1) % size == 0 || (valz + n1 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorMain);
                            } else {
                                setBlock(result, x, y, z, floorAlt);
                            }
                        } else {
                            boolean found = false;
                            for (double i = n1; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                setBlock(result, x, y, z, floorAlt);
                            } else {
                                if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0) {
                                    setBlock(result, x, y, z, floorMain);
                                } else {
                                    boolean found2 = false;
                                    for (double i = n1; i >= 0; i--) {
                                        if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                            found2 = true;
                                            break;
                                        }
                                    }

                                    if (found2) {
                                        setBlock(result, x, y, z, floorAlt);
                                    } else {
                                        boolean found3 = false;
                                        for (double i = n3; i >= 0; i--) {
                                            if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0) {
                                                found3 = true;
                                                break;
                                            }
                                        }

                                        if (found3) {
                                            setBlock(result, x, y, z, floorAlt);
                                        } else {
                                            setBlock(result, x, y, z, plotfloor);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (y == (roadheight + 1)) {

                        if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
                        {
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                            } else {
                                setBlock(result, x, y, z, wall);
                            }
                        } else {
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0) {
                                    setBlock(result, x, y, z, wall);
                                } else {
                                }
                            }
                        }
                    } else {
                        setBlock(result, x, y, z, filling);
                    }
                }
            }
        }
        return result;
    }
}
