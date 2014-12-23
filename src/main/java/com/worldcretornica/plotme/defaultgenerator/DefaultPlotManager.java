package com.worldcretornica.plotme.defaultgenerator;

import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.*;

public class DefaultPlotManager extends BukkitAbstractGenManager {

    public DefaultPlotManager(DefaultGenerator instance) {
        super(instance);
    }

    @Override
    public String getPlotId(Location loc) {
        WorldGenConfig wgc = getWGC(loc.getWorld());

        if (wgc == null) {
            return "";
        }

        int posx = loc.getBlockX();
        int posz = loc.getBlockZ();
        int pathsize = wgc.getInt(PATH_WIDTH);
        int size = wgc.getInt(PLOT_SIZE) + pathsize;

        int xmod = posx % size;
        int zmod = posz % size;

        // negative location
        if (xmod < 0) xmod += size;
        if (zmod < 0) zmod += size;

        // SouthEast plot corner
        int secorner = size - (int) Math.floor(pathsize / 2) - 1;
        // NorthWest plot corner
        int nwcorner = (int) Math.ceil(pathsize / 2) + 1;

        // are we inside or outside the plot?
        if (nwcorner <= xmod && xmod <= secorner &&
            nwcorner <= zmod && zmod <= secorner) {

            // Division needs to use floats.
            // Otherwise it converts the quotient to int, rendering Math.floor useless
            // If we use ints, we will end up with 4x 0;0 plots
            // adding 1 for backwards compatibility with old PlotMe versions
            double idx = 1 + Math.floor((float)posx / (float)size);
            double idz = 1 + Math.floor((float)posz / (float)size);
            return  (int) idx + ";" + (int) idz;
        } else {
            // We hit the road, Jack!
            return "";
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillroad(String id1, String id2, World world) {
        Location bottomPlot1 = getPlotBottomLoc(world, id1);
        Location topPlot1 = getPlotTopLoc(world, id1);
        Location bottomPlot2 = getPlotBottomLoc(world, id2);
        Location topPlot2 = getPlotTopLoc(world, id2);

        int minX;
        int maxX;
        int minZ;
        int maxZ;

        WorldGenConfig wgc = getWGC(world);
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
                for (int y = h; y < 256; y++) {
                    if (y >= (h + 2)) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    } else if (y == (h + 1)) {
                        if (isWallX && (x == minX || x == maxX) || !isWallX && (z == minZ || z == maxZ)) {
                            world.getBlockAt(x, y, z).setTypeIdAndData(wallId, wallValue, true);
                        } else {
                            world.getBlockAt(x, y, z).setType(Material.AIR);
                        }
                    } else {
                        world.getBlockAt(x, y, z).setTypeIdAndData(fillId, fillValue, true);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillmiddleroad(String id1, String id2, World world) {
        Location bottomPlot1 = getPlotBottomLoc(world, id1);
        Location topPlot1 = getPlotTopLoc(world, id1);
        Location bottomPlot2 = getPlotBottomLoc(world, id2);
        Location topPlot2 = getPlotTopLoc(world, id2);

        WorldGenConfig wgc = getWGC(world);
        int height = wgc.getInt(GROUND_LEVEL);
        int fillId = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getId();

        int minX = Math.min(topPlot1.getBlockX(), topPlot2.getBlockX());
        int maxX = Math.max(bottomPlot1.getBlockX(), bottomPlot2.getBlockX());

        int minZ = Math.min(topPlot1.getBlockZ(), topPlot2.getBlockZ());
        int maxZ = Math.max(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = height; y < 256; y++) {
                    if (y >= (height + 1)) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    } else {
                        world.getBlockAt(x, y, z).setTypeId(fillId);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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
        BukkitBlockRepresentation fillBlock = wgc.getBlockRepresentation(FILL_BLOCK);
        BukkitBlockRepresentation floorBlock = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK);

        int bottomX = bottom.getBlockX();
        int topX = top.getBlockX();
        int bottomZ = bottom.getBlockZ();
        int topZ = top.getBlockZ();

        World world = bottom.getWorld();

        clearEntities(bottom, top);

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                Block block = world.getBlockAt(x, 0, z);
                if (!block.getType().equals(Material.BEDROCK)) {
                    block.setType(Material.BEDROCK);
                }
                block.setBiome(Biome.PLAINS);

                for (int y = 256; y >= 0; y--) {
                    block = world.getBlockAt(x, y, z);

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

                    if (y < roadHeight) {
                        if (block.getTypeId() != (int) fillBlock.getId()) {
                            block.setTypeIdAndData(fillBlock.getId(), fillBlock.getData(), true);
                        }
                    } else if (y == roadHeight) {
                        if (block.getTypeId() != (int) floorBlock.getId()) {
                            block.setTypeIdAndData(floorBlock.getId(), floorBlock.getData(), true);
                        }
                    } else if (y != (roadHeight + 1) || (x != bottomX - 1 && x != topX + 1 && z != bottomZ - 1 && z != topZ + 1)) {
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Long[] clear(Location bottom, Location top, long maxBlocks, Long[] start) {
        clearEntities(bottom, top);

        WorldGenConfig wgc = getWGC(bottom.getWorld());
        int roadHeight = wgc.getInt(GROUND_LEVEL);
        BukkitBlockRepresentation fillBlock = wgc.getBlockRepresentation(FILL_BLOCK);
        BukkitBlockRepresentation floorBlock = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK);

        int bottomX;
        int topX = top.getBlockX();
        int bottomZ;
        int topZ = top.getBlockZ();

        long nbBlockClearedBefore = 0;

        World world = bottom.getWorld();

        if (start == null) {
            bottomX = bottom.getBlockX();
            bottomZ = bottom.getBlockZ();
        } else {
            bottomX = start[0].intValue();
            bottomZ = start[2].intValue();
            nbBlockClearedBefore = start[3];
        }

        long nbBlockCleared = 0;
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                Block block = world.getBlockAt(x, 0, z);
                if (!block.getType().equals(Material.BEDROCK)) {
                    block.setType(Material.BEDROCK);
                }
                block.setBiome(Biome.PLAINS);

                for (int y = 0; y < 256 + 1; y++) {
                    block = world.getBlockAt(x, y, z);

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

                    if (y < roadHeight) {
                        if (block.getTypeId() != (int) fillBlock.getId()) {
                            block.setTypeIdAndData(fillBlock.getId(), fillBlock.getData(), true);
                        }
                    } else if (y == roadHeight) {
                        if (block.getTypeId() != (int) floorBlock.getId()) {
                            block.setTypeIdAndData(floorBlock.getId(), floorBlock.getData(), true);
                        }
                    } else if (y != (roadHeight + 1) || (x != bottomX - 1 && x != topX + 1 && z != bottomZ - 1 && z != topZ + 1)) {
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                        }
                    }

                    nbBlockCleared++;

                    if (nbBlockCleared >= maxBlocks) {
                        return new Long[]{(long) x, (long) y, (long) z, nbBlockClearedBefore + nbBlockCleared};
                    }
                }
            }
            bottomZ = bottom.getBlockZ();
        }

        return null;
    }

    @Override
    public void adjustPlotFor(World world, String id, boolean claimed, boolean protect, boolean auctioned, boolean forSale) {
        WorldGenConfig wgc = getWGC(world);

        List<String> wallids = new ArrayList<>();

        int roadHeight = wgc.getInt(GROUND_LEVEL);

        String wallid = wgc.getString(WALL_BLOCK);
        String protectedwallid = wgc.getString(PROTECTED_WALL_BLOCK);
        String auctionwallid = wgc.getString(AUCTION_WALL_BLOCK);
        String forsalewallid = wgc.getString(FOR_SALE_WALL_BLOCK);

        if (protect) {
            wallids.add(protectedwallid);
        }
        if (auctioned && !wallids.contains(auctionwallid)) {
            wallids.add(auctionwallid);
        }
        if (forSale && !wallids.contains(forsalewallid)) {
            wallids.add(forsalewallid);
        }

        if (wallids.isEmpty()) {
            wallids.add(wallid);
        }

        int ctr = 0;

        Location bottom = getPlotBottomLoc(world, id);
        Location top = getPlotTopLoc(world, id);

        int x;
        int z;

        String currentblockid;
        Block block;

        for (x = bottom.getBlockX() - 1; x < top.getBlockX() + 1; x++) {
            z = bottom.getBlockZ() - 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }

        for (z = bottom.getBlockZ() - 1; z < top.getBlockZ() + 1; z++) {
            x = top.getBlockX() + 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }

        for (x = top.getBlockX() + 1; x > bottom.getBlockX() - 1; x--) {
            z = top.getBlockZ() + 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }

        for (z = top.getBlockZ() + 1; z > bottom.getBlockZ() - 1; z--) {
            x = bottom.getBlockX() - 1;
            currentblockid = wallids.get(ctr);
            ctr = (ctr == wallids.size() - 1) ? 0 : ctr + 1;
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentblockid);
        }
    }

    @SuppressWarnings("deprecation")
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
    public Location getPlotHome(World world, String id) {
        WorldGenConfig wgc = getWGC(world);

        if (wgc != null) {
            return new Location(world, bottomX(id, world) + (topX(id, world) - bottomX(id, world)) / 2, wgc.getInt(GROUND_LEVEL) + 2, bottomZ(id, world) - 2);
        } else {
            return world.getSpawnLocation();
        }
    }
}
