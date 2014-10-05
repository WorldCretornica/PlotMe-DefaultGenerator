package com.worldcretornica.plotme.defaultgenerator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.InventoryHolder;

import com.worldcretornica.plotme_abstractgenerator.AbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.BlockRepresentation;
import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import com.worldcretornica.plotme_core.api.ILocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.*;

public class DefaultPlotManager extends AbstractGenManager {

    public DefaultPlotManager(DefaultGenerator instance) {
        super(instance);
    }

    @Override
    public String getPlotId(ILocation loc) {
        WorldGenConfig wgc = getWGC(loc.getWorld());

        if (wgc != null) {
            int valx = loc.getBlockX();
            int valz = loc.getBlockZ();

            int pathsize = wgc.getInt(PATH_WIDTH);
            int size = wgc.getInt(PLOT_SIZE) + pathsize;
            boolean road = false;

            double n3;
            int mod2 = 0;
            int mod1 = 1;

            int x = (int) Math.ceil((double) valx / size);
            int z = (int) Math.ceil((double) valz / size);

            //int x2 = (int) Math.ceil((double)valx / size);
            //int z2 = (int) Math.ceil((double)valz / size);
            if (pathsize % 2 == 1) {
                n3 = Math.ceil(((double) pathsize) / 2); //3 7
                mod2 = -1;
            } else {
                n3 = Math.floor(((double) pathsize) / 2); //3 7
            }

            for (double i = n3; i >= 0; i--) {
                if ((valx - i + mod1) % size == 0
                        || (valx + i + mod2) % size == 0) {
                    road = true;

                    x = (int) Math.ceil((valx - n3) / size);
                    //x2 = (int) Math.ceil((double)(valx + n3) / size);
                }
                if ((valz - i + mod1) % size == 0
                        || (valz + i + mod2) % size == 0) {
                    road = true;

                    z = (int) Math.ceil((valz - n3) / size);
                    //z2 = (int) Math.ceil((double)(valz + n3) / size);
                }
            }

            if (road) {
                /*if(pmi.AutoLinkPlots)
                 {
                 String id1 = x + ";" + z;
                 String id2 = x2 + ";" + z2;
                 String id3 = x + ";" + z2;
                 String id4 = x2 + ";" + z;

                 HashMap<String, Plot> plots = pmi.plots;

                 Plot p1 = plots.get(id1);
                 Plot p2 = plots.get(id2);
                 Plot p3 = plots.get(id3);
                 Plot p4 = plots.get(id4);

                 if(p1 == null || p2 == null || p3 == null || p4 == null ||
                 !p1.owner.equalsIgnoreCase(p2.owner) ||
                 !p2.owner.equalsIgnoreCase(p3.owner) ||
                 !p3.owner.equalsIgnoreCase(p4.owner))
                 {
                 return "";
                 }
                 else
                 {
                 return id1;
                 }
                 }
                 else*/
                return "";
            } else {
                return "" + x + ";" + z;
            }
        } else {
            return "";
        }
    }

    @Override
    public void fillroad(String id1, String id2, World w) {
        Location bottomPlot1 = getPlotBottomLoc(w, id1);
        Location topPlot1 = getPlotTopLoc(w, id1);
        Location bottomPlot2 = getPlotBottomLoc(w, id2);
        Location topPlot2 = getPlotTopLoc(w, id2);

        int minX;
        int maxX;
        int minZ;
        int maxZ;

        WorldGenConfig wgc = getWGC(w);
        int h = wgc.getInt(GROUND_LEVEL);
        int wallId = wgc.getBlockRepresentation(WALL_BLOCK).getId();
        byte wallValue = wgc.getBlockRepresentation(WALL_BLOCK).getData();
        int fillId = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getId();
        byte fillValue = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getData();
        int plotSize = wgc.getInt(PLOT_SIZE);

        if (bottomPlot1.getBlockX() == bottomPlot2.getBlockX()) {
            minX = bottomPlot1.getBlockX();
            maxX = topPlot1.getBlockX();

            minZ = Math.min(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ()) + plotSize;
            maxZ = Math.max(topPlot1.getBlockZ(), topPlot2.getBlockZ()) - plotSize;
        } else {
            minZ = bottomPlot1.getBlockZ();
            maxZ = topPlot1.getBlockZ();

            minX = Math.min(bottomPlot1.getBlockX(), bottomPlot2.getBlockX()) + plotSize;
            maxX = Math.max(topPlot1.getBlockX(), topPlot2.getBlockX()) - plotSize;
        }

        boolean isWallX = (maxX - minX) > (maxZ - minZ);

        if (isWallX) {
            minX--;
            maxX++;
        } else {
            minZ--;
            maxZ++;
        }

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = h; y < w.getMaxHeight(); y++) {
                    if (y >= (h + 2)) {
                        w.getBlockAt(x, y, z).setType(Material.AIR);
                    } else if (y == (h + 1)) {
                        if (isWallX && (x == minX || x == maxX) || !isWallX && (z == minZ || z == maxZ)) {
                            w.getBlockAt(x, y, z).setTypeIdAndData(wallId, wallValue, true);
                        } else {
                            w.getBlockAt(x, y, z).setType(Material.AIR);
                        }
                    } else {
                        w.getBlockAt(x, y, z).setTypeIdAndData(fillId, fillValue, true);
                    }
                }
            }
        }
    }

    @Override
    public void fillmiddleroad(String id1, String id2, World w) {
        Location bottomPlot1 = getPlotBottomLoc(w, id1);
        Location topPlot1 = getPlotTopLoc(w, id1);
        Location bottomPlot2 = getPlotBottomLoc(w, id2);
        Location topPlot2 = getPlotTopLoc(w, id2);

        int minX;
        int maxX;
        int minZ;
        int maxZ;

        WorldGenConfig wgc = getWGC(w);
        int h = wgc.getInt(GROUND_LEVEL);
        int fillId = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getId();

        minX = Math.min(topPlot1.getBlockX(), topPlot2.getBlockX());
        maxX = Math.max(bottomPlot1.getBlockX(), bottomPlot2.getBlockX());

        minZ = Math.min(topPlot1.getBlockZ(), topPlot2.getBlockZ());
        maxZ = Math.max(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = h; y < w.getMaxHeight(); y++) {
                    if (y >= (h + 1)) {
                        w.getBlockAt(x, y, z).setType(Material.AIR);
                    } else {
                        w.getBlockAt(x, y, z).setTypeId(fillId);
                    }
                }
            }
        }
    }

    @Override
    public void setOwnerDisplay(World world, String id, String line1, String line2, String line3, String line4) {
        Location pillar = new Location(world, bottomX(id, world) - 1, getWGC(world).getInt(GROUND_LEVEL) + 1, bottomZ(id, world) - 1);

        Block bsign = pillar.clone().add(0, 0, -1).getBlock();
        bsign.setType(Material.AIR);
        bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);

        Sign sign = (Sign) bsign.getState();

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);

        sign.update(true);
    }

    @Override
    public void setSellerDisplay(World world, String id, String line1, String line2, String line3, String line4) {
        removeSellerDisplay(world, id);

        Location pillar = new Location(world, bottomX(id, world) - 1, getWGC(world).getInt(GROUND_LEVEL) + 1, bottomZ(id, world) - 1);

        Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
        bsign.setType(Material.AIR);
        bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 4, false);

        Sign sign = (Sign) bsign.getState();

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);

        sign.update(true);
    }

    @Override
    public void setAuctionDisplay(World world, String id, String line1, String line2, String line3, String line4) {
        removeSellerDisplay(world, id);

        Location pillar = new Location(world, bottomX(id, world) - 1, getWGC(world).getInt(GROUND_LEVEL) + 1, bottomZ(id, world) - 1);

        Block bsign = pillar.clone().add(-1, 0, 1).getBlock();
        bsign.setType(Material.AIR);
        bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 4, false);

        Sign sign = (Sign) bsign.getState();

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);

        sign.update(true);
    }

    @Override
    public void removeOwnerDisplay(World world, String id) {
        Location bottom = getPlotBottomLoc(world, id);

        Location pillar = new Location(world, bottom.getX() - 1, getWGC(world).getInt(GROUND_LEVEL) + 1, bottom.getZ() - 1);

        Block bsign = pillar.add(0, 0, -1).getBlock();
        bsign.setType(Material.AIR);
    }

    @Override
    public void removeSellerDisplay(World world, String id) {
        Location bottom = getPlotBottomLoc(world, id);

        Location pillar = new Location(world, bottom.getX() - 1, getWGC(world).getInt(GROUND_LEVEL) + 1, bottom.getZ() - 1);

        Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
        bsign.setType(Material.AIR);

        //bsign = pillar.clone().add(-1, 0, 1).getBlock();
        //bsign.setType(Material.AIR);
    }

    @Override
    public void removeAuctionDisplay(World world, String id) {
        Location bottom = getPlotBottomLoc(world, id);

        Location pillar = new Location(world, bottom.getX() - 1, getWGC(world).getInt(GROUND_LEVEL) + 1, bottom.getZ() - 1);

        //Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
        //bsign.setType(Material.AIR);
        Block bsign = pillar.clone().add(-1, 0, 1).getBlock();
        bsign.setType(Material.AIR);
    }

    @Override
    public Location getPlotBottomLoc(World world, String id) {
        int px = getIdX(id);
        int pz = getIdZ(id);

        WorldGenConfig wgc = getWGC(world);
        int plotSize = wgc.getInt(PLOT_SIZE);
        int pathWidth = wgc.getInt(PATH_WIDTH);

        int x = px * (plotSize + pathWidth) - (plotSize) - ((int) Math.floor(pathWidth / 2));
        int z = pz * (plotSize + pathWidth) - (plotSize) - ((int) Math.floor(pathWidth / 2));

        return new Location(world, x, 1, z);
    }

    @Override
    public Location getPlotTopLoc(World world, String id) {
        int px = getIdX(id);
        int pz = getIdZ(id);

        WorldGenConfig wgc = getWGC(world);
        int plotSize = wgc.getInt(PLOT_SIZE);
        int pathWidth = wgc.getInt(PATH_WIDTH);

        int x = px * (plotSize + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;
        int z = pz * (plotSize + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;

        return new Location(world, x, 255, z);
    }

    @Override
    public void clear(Location bottom, Location top) {
        WorldGenConfig wgc = getWGC(bottom.getWorld());
        int roadHeight = wgc.getInt(GROUND_LEVEL);
        BlockRepresentation bottomBlock = wgc.getBlockRepresentation(BASE_BLOCK);
        BlockRepresentation fillBlock = wgc.getBlockRepresentation(FILL_BLOCK);
        BlockRepresentation floorBlock = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK);

        int bottomX = bottom.getBlockX();
        int topX = top.getBlockX();
        int bottomZ = bottom.getBlockZ();
        int topZ = top.getBlockZ();

        World w = bottom.getWorld();

        clearEntities(bottom, top);

        int maxY = w.getMaxHeight();

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                Block block = w.getBlockAt(x, 0, z);

                block.setBiome(Biome.PLAINS);

                for (int y = maxY; y >= 0; y--) {
                    block = w.getBlockAt(x, y, z);

                    if (block.getType() == Material.BEACON
                            || block.getType() == Material.CHEST
                            || block.getType() == Material.BREWING_STAND
                            || block.getType() == Material.DISPENSER
                            || block.getType() == Material.FURNACE
                            || block.getType() == Material.DROPPER
                            || block.getType() == Material.HOPPER) {
                        InventoryHolder holder = (InventoryHolder) block.getState();
                        holder.getInventory().clear();
                    }

                    if (block.getType() == Material.JUKEBOX) {
                        Jukebox jukebox = (Jukebox) block.getState();
                        //Remove once they fix the NullPointerException
                        try {
                            jukebox.setPlaying(Material.AIR);
                        } catch (Exception e) {
                        }
                    }

                    if (y == 0) {
                        bottomBlock.setBlock(block);
                    } else if (y < roadHeight) {
                        fillBlock.setBlock(block);
                    } else if (y == roadHeight) {
                        floorBlock.setBlock(block);
                    } else {
                        if (y == (roadHeight + 1)
                                && (x == bottomX - 1
                                || x == topX + 1
                                || z == bottomZ - 1
                                || z == topZ + 1)) {
                            //block.setTypeId(pmi.WallBlockId);
                        } else {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Long[] clear(Location bottom, Location top, long maxBlocks, boolean clearEntities, Long[] start) {
        if (clearEntities) {
            clearEntities(bottom, top);
        }

        WorldGenConfig wgc = getWGC(bottom.getWorld());
        int roadHeight = wgc.getInt(GROUND_LEVEL);
        BlockRepresentation bottomBlock = wgc.getBlockRepresentation(BASE_BLOCK);
        BlockRepresentation fillBlock = wgc.getBlockRepresentation(FILL_BLOCK);
        BlockRepresentation floorBlock = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK);

        int bottomX;
        int topX = top.getBlockX();
        int bottomZ;
        int topZ = top.getBlockZ();
        int maxY;

        long nbBlockCleared = 0;
        long nbBlockClearedBefore = 0;

        World w = bottom.getWorld();

        if (start == null) {
            bottomX = bottom.getBlockX();
            maxY = w.getMaxHeight();
            bottomZ = bottom.getBlockZ();
        } else {
            bottomX = start[0].intValue();
            maxY = start[1].intValue() - 1;
            bottomZ = start[2].intValue();
            nbBlockClearedBefore = start[3];
        }

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                Block block = w.getBlockAt(x, 0, z);

                block.setBiome(Biome.PLAINS);

                for (int y = maxY; y >= 0; y--) {
                    block = w.getBlockAt(x, y, z);

                    if (block.getType() == Material.BEACON
                            || block.getType() == Material.CHEST
                            || block.getType() == Material.BREWING_STAND
                            || block.getType() == Material.DISPENSER
                            || block.getType() == Material.FURNACE
                            || block.getType() == Material.DROPPER
                            || block.getType() == Material.HOPPER) {
                        InventoryHolder holder = (InventoryHolder) block.getState();
                        holder.getInventory().clear();
                    }

                    if (block.getType() == Material.JUKEBOX) {
                        Jukebox jukebox = (Jukebox) block.getState();
                        //Remove once they fix the NullPointerException
                        try {
                            jukebox.setPlaying(Material.AIR);
                        } catch (Exception e) {
                        }
                    }

                    if (y == 0) {
                        bottomBlock.setBlock(block);
                    } else if (y < roadHeight) {
                        fillBlock.setBlock(block);
                    } else if (y == roadHeight) {
                        floorBlock.setBlock(block);
                    } else {
                        if (y == (roadHeight + 1)
                                && (x == bottomX - 1
                                || x == topX + 1
                                || z == bottomZ - 1
                                || z == topZ + 1)) {
                            //block.setTypeId(pmi.WallBlockId);
                        } else {
                            //block.setTypeIdAndData(0, (byte) 0, false); //.setType(Material.AIR);
                            block.setType(Material.AIR);
                        }
                    }

                    nbBlockCleared++;

                    if (nbBlockCleared >= maxBlocks) {
                        return new Long[]{(long) x, (long) y, (long) z, nbBlockClearedBefore + nbBlockCleared};
                    }
                }
                maxY = w.getMaxHeight();
            }
            bottomZ = bottom.getBlockZ();
        }

        return null;
    }

    @Override
    public void adjustPlotFor(World w, String id, boolean Claimed, boolean Protected, boolean Auctionned, boolean ForSale) {
        WorldGenConfig wgc = getWGC(w);

        List<String> wallids = new ArrayList<>();

        int roadHeight = wgc.getInt(GROUND_LEVEL);

        String wallid = wgc.getString(WALL_BLOCK);
        String protectedwallid = wgc.getString(PROTECTED_WALL_BLOCK);
        String auctionwallid = wgc.getString(AUCTION_WALL_BLOCK);
        String forsalewallid = wgc.getString(FOR_SALE_WALL_BLOCK);

        if (Protected) {
            wallids.add(protectedwallid);
        }
        if (Auctionned && !wallids.contains(auctionwallid)) {
            wallids.add(auctionwallid);
        }
        if (ForSale && !wallids.contains(forsalewallid)) {
            wallids.add(forsalewallid);
        }

        if (wallids.isEmpty()) {
            wallids.add(wallid);
        }

        int ctr = 0;

        Location bottom = getPlotBottomLoc(w, id);
        Location top = getPlotTopLoc(w, id);

        int x;
        int z;

        String currentblockid;
        Block block;

        for (x = bottom.getBlockX() - 1; x < top.getBlockX() + 1; x++) {
            z = bottom.getBlockZ() - 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = w.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }

        for (z = bottom.getBlockZ() - 1; z < top.getBlockZ() + 1; z++) {
            x = top.getBlockX() + 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = w.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }

        for (x = top.getBlockX() + 1; x > bottom.getBlockX() - 1; x--) {
            z = top.getBlockZ() + 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = w.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }

        for (z = top.getBlockZ() + 1; z > bottom.getBlockZ() - 1; z--) {
            x = bottom.getBlockX() - 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = w.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }
    }

    private void setWall(Block block, String currentblockid) {

        int blockId;
        byte blockData = 0;
        WorldGenConfig wgc = getWGC(block.getWorld());

        if (currentblockid.contains(":")) {
            try {
                blockId = Integer.parseInt(currentblockid.substring(0, currentblockid.indexOf(":")));
                blockData = Byte.parseByte(currentblockid.substring(currentblockid.indexOf(":") + 1));
            } catch (NumberFormatException e) {
                blockId = wgc.getBlockRepresentation(WALL_BLOCK).getId();
                blockData = wgc.getBlockRepresentation(WALL_BLOCK).getData();
            }
        } else {
            try {
                blockId = Integer.parseInt(currentblockid);
            } catch (NumberFormatException e) {
                blockId = wgc.getBlockRepresentation(WALL_BLOCK).getId();
            }
        }

        block.setTypeIdAndData(blockId, blockData, true);
    }

    @Override
    public void regen(World w, String id, CommandSender sender) {
        int bottomX = bottomX(id, w);
        int topX = topX(id, w);
        int bottomZ = bottomZ(id, w);
        int topZ = topZ(id, w);

        int minChunkX = (int) Math.floor((double) bottomX / 16);
        int maxChunkX = (int) Math.floor((double) topX / 16);
        int minChunkZ = (int) Math.floor((double) bottomZ / 16);
        int maxChunkZ = (int) Math.floor((double) topZ / 16);

        HashMap<Location, Biome> biomes = new HashMap<>();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            int xx = cx << 4;

            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                int zz = cz << 4;

                BlockState[][][] blocks = new BlockState[16][16][w.getMaxHeight()];

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        biomes.put(new Location(w, x + xx, 0, z + zz), w.getBiome(x + xx, z + zz));

                        for (int y = 0; y < w.getMaxHeight(); y++) {
                            Block block = w.getBlockAt(x + xx, y, z + zz);
                            blocks[x][z][y] = block.getState();
                        }
                    }
                }

                w.regenerateChunk(cx, cz);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < w.getMaxHeight(); y++) {
                            if ((x + xx) < bottomX || (x + xx) > topX || (z + zz) < bottomZ || (z + zz) > topZ) {
                                Block newblock = w.getBlockAt(x + xx, y, z + zz);
                                BlockState oldblock = blocks[x][z][y];

                                newblock.setTypeIdAndData(oldblock.getTypeId(), oldblock.getRawData(), false);
                                oldblock.update();
                            }
                        }
                    }
                }
            }
        }

        for (Location loc : biomes.keySet()) {
            int x = loc.getBlockX();
            int z = loc.getBlockX();

            w.setBiome(x, z, biomes.get(loc));
        }
    }

    @Override
    public Location getPlotHome(World w, String id) {
        WorldGenConfig wgc = getWGC(w);

        if (wgc != null) {
            return new Location(w, bottomX(id, w) + (topX(id, w) - bottomX(id, w)) / 2, wgc.getInt(GROUND_LEVEL) + 2, bottomZ(id, w) - 2);
        } else {
            return w.getSpawnLocation();
        }
    }
}
