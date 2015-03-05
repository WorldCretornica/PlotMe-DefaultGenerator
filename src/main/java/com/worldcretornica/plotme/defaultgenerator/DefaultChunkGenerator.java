package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FILL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_FLOOR_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_SIZE;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_ALT_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.ROAD_MAIN_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.UNCLAIMED_WALL;
import static com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath.X_TRANSLATION;
import static com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath.Z_TRANSLATION;

import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DefaultChunkGenerator extends ChunkGenerator {

    private final List<BlockPopulator> blockPopulators = new ArrayList<>();
    private final WorldGenConfig wgc;

    public DefaultChunkGenerator(DefaultGenerator instance, String worldName) {
        wgc = instance.getGeneratorManager().getWGC(worldName);
        blockPopulators.add(new DefaultRoadPopulator(wgc));
        blockPopulators.add(new DefaultContentPopulator(wgc));
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return blockPopulators;
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes) {
        int plotSize = wgc.getInt(PLOT_SIZE);
        int pathSize = wgc.getInt(PATH_WIDTH);
        int roadHeight = wgc.getInt(GROUND_LEVEL);
        short wall = wgc.getBlockRepresentation(UNCLAIMED_WALL).getId();
        short floorMain = wgc.getBlockRepresentation(ROAD_MAIN_BLOCK).getId();
        short floorAlt = wgc.getBlockRepresentation(ROAD_ALT_BLOCK).getId();
        short plotFloor = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getId();
        short filling = wgc.getBlockRepresentation(FILL_BLOCK).getId();

        double size = plotSize + pathSize;

        double n1;
        double n2;
        double n3;
        int mod2 = 0;

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

        int mod1 = 1;
        short[][] result = new short[16][];
        for (int x = 0; x < 16; x++) {
            int valx = (cx << 4) + x;

            for (int z = 0; z < 16; z++) {
                int height = roadHeight + 2;
                int valz = (cz << 4) + z;

                setBlock(result, x, 0, z, (short) 7);
                biomes.setBiome(x, z, Biome.PLAINS);

                for (int y = 1; y < height; y++) {
                    if (y == roadHeight) {
                        if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) {//middle+3
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                setBlock(result, x, y, z, floorMain);
                            } else {
                                setBlock(result, x, y, z, filling);
                            }
                        } else if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
                        {
                            if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
                                || (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorMain);
                            } else {
                                setBlock(result, x, y, z, floorAlt);
                            }
                        } else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
                        {
                            if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 || (valz - n1 + mod1) % size == 0
                                    || (valz + n1 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorAlt);
                            } else {
                                setBlock(result, x, y, z, floorMain);
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
                                setBlock(result, x, y, z, floorMain);
                            } else if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorAlt);
                            } else {
                                boolean found2 = false;
                                for (double i = n1; i >= 0; i--) {
                                    if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                        found2 = true;
                                        break;
                                    }
                                }

                                if (found2) {
                                    setBlock(result, x, y, z, floorMain);
                                } else {
                                    boolean found3 = false;
                                    for (double i = n3; i >= 0; i--) {
                                        if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0) {
                                            found3 = true;
                                            break;
                                        }
                                    }

                                    if (found3) {
                                        setBlock(result, x, y, z, floorMain);
                                    } else {
                                        setBlock(result, x, y, z, plotFloor);
                                    }
                                }
                            }
                        }
                    } else if (y == (roadHeight + 1)) {
                        if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
                        {
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
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

                            if (!found && ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0)) {
                                setBlock(result, x, y, z, wall);
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
    
    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, wgc.getInt(X_TRANSLATION), wgc.getInt(GROUND_LEVEL) + 2, wgc.getInt(Z_TRANSLATION));
    }

    protected void setBlock(short[][] result, int x, int y, int z, short blockId) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][(y & 0xF) * 256 | (z << 4) | x] = blockId;
    }
}
