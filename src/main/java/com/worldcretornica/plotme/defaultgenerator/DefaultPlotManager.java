package com.worldcretornica.plotme.defaultgenerator;

import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotId;
import com.worldcretornica.plotme_core.api.IBlock;
import com.worldcretornica.plotme_core.api.IWorld;
import com.worldcretornica.plotme_core.api.Location;
import com.worldcretornica.plotme_core.api.Vector;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class DefaultPlotManager extends BukkitAbstractGenManager {

    public DefaultPlotManager(BukkitDefaultGenerator instance, ConfigurationSection wgc, IWorld world) {
        super(instance, wgc, world);
    }

    @Override
    public PlotId getPlotId(Vector loc) {
        int posx = loc.getBlockX();
        int posz = loc.getBlockZ();
        int pathSize = wgc.getInt(DefaultWorldConfigPath.PATH_WIDTH.key());
        int size = getPlotSize() + pathSize;

        return internalgetPlotId(pathSize, size, posx, posz);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillRoad(PlotId id1, PlotId id2) {
        Vector bottomPlot1 = getPlotBottomLoc(id1);
        Vector topPlot1 = getPlotTopLoc(id1);
        Vector bottomPlot2 = getPlotBottomLoc(id2);
        Vector topPlot2 = getPlotTopLoc(id2);

        int minX;
        int maxX;
        int minZ;
        int maxZ;

        int h = getGroundHeight();
        short wallId = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.UNCLAIMED_WALL.key(), "44:7"));
        byte wallValue = BukkitBlockRepresentation.getBlockData(wgc.getString(DefaultWorldConfigPath.UNCLAIMED_WALL.key(), "44:7"));
        short fillId = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.FILL_BLOCK.key(), "3"));
        byte fillValue = BukkitBlockRepresentation.getBlockData(wgc.getString(DefaultWorldConfigPath.FILL_BLOCK.key(), "3"));

        if (bottomPlot1.getBlockX() == bottomPlot2.getBlockX()) {
            minX = bottomPlot1.getBlockX();
            maxX = topPlot1.getBlockX();

            minZ = Math.min(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ()) + getPlotSize();
            maxZ = Math.max(topPlot1.getBlockZ(), topPlot2.getBlockZ()) - getPlotSize();
        } else {
            minZ = bottomPlot1.getBlockZ();
            maxZ = topPlot1.getBlockZ();

            minX = Math.min(bottomPlot1.getBlockX(), bottomPlot2.getBlockX()) + getPlotSize();
            maxX = Math.max(topPlot1.getBlockX(), topPlot2.getBlockX()) - getPlotSize();
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
                        world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    } else if (y == (h + 1)) {
                        if (isWallX && (x == minX || x == maxX) || !isWallX && (z == minZ || z == maxZ)) {
                            world.getBlockAt(x, y, z).setTypeIdAndData(wallId, wallValue, false);
                        } else {
                            world.getBlockAt(x, y, z).setType(Material.AIR, false);
                        }
                    } else {
                        world.getBlockAt(x, y, z).setTypeIdAndData(fillId, fillValue, false);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillMiddleRoad(PlotId id1, PlotId id2) {
        Vector bottomPlot1 = getPlotBottomLoc(id1);
        Vector topPlot1 = getPlotTopLoc(id1);
        Vector bottomPlot2 = getPlotBottomLoc(id2);
        Vector topPlot2 = getPlotTopLoc(id2);

        int height = getGroundHeight();
        short fillId = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.PLOT_FLOOR_BLOCK.key()));

        int minX = Math.min(topPlot1.getBlockX(), topPlot2.getBlockX());
        int maxX = Math.max(bottomPlot1.getBlockX(), bottomPlot2.getBlockX());

        int minZ = Math.min(topPlot1.getBlockZ(), topPlot2.getBlockZ());
        int maxZ = Math.max(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = height; y < 256; y++) {
                    if (y >= (height + 1)) {
                        world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    } else {
                        world.getBlockAt(x, y, z).setTypeId(fillId, false);
                    }
                }
            }
        }
    }

    @Override
    public void setOwnerDisplay(PlotId id, String line1, String line2, String line3, String line4) {
        Location pillar = new Location(world, bottomX(id) - 1, getGroundHeight() + 1, bottomZ(id) - 1);
        IBlock bsign = pillar.add(0, 0, -1).getBlock();
        bsign.setType(Material.AIR, false);
        bsign.setTypeIdAndData((short) Material.WALL_SIGN.getId(), (byte) 2, false);

        Sign sign = (Sign) bsign.getState();

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);

        sign.update(true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setSellerDisplay(PlotId id, String line1, String line2, String line3, String line4) {
        removeSellerDisplay(id);

        Location pillar = new Location(world, bottomX(id) - 1, getGroundHeight() + 1, bottomZ(id) - 1);

        IBlock bsign = pillar.add(-1, 0, 0).getBlock();
        bsign.setType(Material.AIR, false);
        bsign.setTypeIdAndData((short) Material.WALL_SIGN.getId(), (byte) 4, false);

        Sign sign = (Sign) bsign.getState();

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);

        sign.update(true);
    }

    @Override
    public void removeOwnerDisplay(PlotId id) {
        Vector bottom = getPlotBottomLoc(id);

        Location pillar = new Location(world, bottom.getX() - 1, getGroundHeight() + 1, bottom.getZ() - 1);

        IBlock bsign = pillar.add(0, 0, -1).getBlock();
        bsign.setType(Material.AIR, false);
    }

    @Override
    public void removeSellerDisplay(PlotId id) {
        Vector bottom = getPlotBottomLoc(id);

        Location pillar = new Location(world, bottom.getX() - 1, getGroundHeight() + 1, bottom.getZ() - 1);

        IBlock bsign = pillar.add(-1, 0, 0).getBlock();
        bsign.setType(Material.AIR, false);

    }
    @Override
    public Vector getPlotBottomLoc(PlotId id) {
        int px = id.getX();
        int pz = id.getZ();

        int pathWidth = wgc.getInt(DefaultWorldConfigPath.PATH_WIDTH.key());

        int x = (px * (getPlotSize() + pathWidth)) - (getPlotSize()) - ((int) Math.floor(pathWidth / 2));
        int z = pz * (getPlotSize() + pathWidth) - (getPlotSize()) - ((int) Math.floor(pathWidth / 2));

        return new Vector(x, 0, z);
    }

    @Override
    public Vector getPlotTopLoc(PlotId id) {
        int px = id.getX();
        int pz = id.getZ();

        int pathWidth = wgc.getInt(DefaultWorldConfigPath.PATH_WIDTH.key());

        int x = px * (getPlotSize() + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;
        int z = pz * (getPlotSize() + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;

        return new Vector(x, 256, z);
    }

    @Override
    public long[] clear(Vector bottom, Vector top, long maxBlocks, long[] start) {
        clearEntities(bottom, top);
        int roadHeight = getGroundHeight();
        BukkitBlockRepresentation fillBlock = new BukkitBlockRepresentation(wgc.getString(DefaultWorldConfigPath.FILL_BLOCK.key()));
        BukkitBlockRepresentation floorBlock = new BukkitBlockRepresentation(wgc.getString(DefaultWorldConfigPath.PLOT_FLOOR_BLOCK.key()));

        int bottomX;
        int topX = top.getBlockX();
        int bottomZ;
        int topZ = top.getBlockZ();

        long nbBlockClearedBefore = 0;

        if (start == null) {
            bottomX = bottom.getBlockX();
            bottomZ = bottom.getBlockZ();
        } else {
            bottomX = (int) start[0];
            bottomZ = (int) start[2];
            nbBlockClearedBefore = start[3];
        }

        long nbBlockCleared = 0;
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                IBlock block = world.getBlockAt(x, 0, z);
                if (!block.getType().equals(Material.BEDROCK)) {
                    block.setType(Material.BEDROCK, false);
                }
                block.setBiome(Biome.PLAINS);

                for (int y = 1; y < 255; y++) {
                    block = world.getBlockAt(x, y, z);
                    if (block.getState() instanceof InventoryHolder) {
                        InventoryHolder holder = (InventoryHolder) block.getState();
                        holder.getInventory().clear();
                    }

                    if (y < roadHeight) {
                        if (block.getTypeId() != (int) fillBlock.getId()) {
                            block.setTypeIdAndData(fillBlock.getId(), fillBlock.getData(), false);
                        }
                    } else if (y == roadHeight) {
                        if (block.getTypeId() != (int) floorBlock.getId()) {
                            block.setTypeIdAndData(floorBlock.getId(), floorBlock.getData(), false);
                        }
                    } else if ((y != roadHeight + 1 || x != bottomX - 1 && x != topX + 1 && z != bottomZ - 1 && z != topZ + 1)
                            && !block.getType().equals(Material.AIR)) {
                        block.setType(Material.AIR, false);
                    }

                    nbBlockCleared++;

                    if (nbBlockCleared >= maxBlocks) {
                        return new long[]{x, y, z, nbBlockClearedBefore + nbBlockCleared};
                    }
                }
            }
            bottomZ = bottom.getBlockZ();
        }

        refreshPlotChunks(getPlotId(bottom));
        return null;
    }

    @Override
    public void adjustPlotFor(Plot plot, boolean claimed, boolean protect, boolean forSale) {
        List<String> wallIds = new ArrayList<>();
        int roadHeight = getGroundHeight();

        String claimedId = wgc.getString(DefaultWorldConfigPath.WALL_BLOCK.key());
        String wallId = wgc.getString(DefaultWorldConfigPath.UNCLAIMED_WALL.key());
        String protectedWallId = wgc.getString(DefaultWorldConfigPath.PROTECTED_WALL_BLOCK.key());
        String forsaleWallId = wgc.getString(DefaultWorldConfigPath.FOR_SALE_WALL_BLOCK.key());

        if (protect) {
            wallIds.add(protectedWallId);
        }
        if (forSale && !wallIds.contains(forsaleWallId)) {
            wallIds.add(forsaleWallId);
        }
        if (claimed && !wallIds.contains(claimedId)) {
            wallIds.add(claimedId);
        }
        if (wallIds.isEmpty()) {
            wallIds.add(wallId);
        }

        int ctr = 0;

        Vector bottom = getPlotBottomLoc(plot.getId());
        Vector top = getPlotTopLoc(plot.getId());

        int x;
        int z;

        String currentBlockId;
        IBlock block;

        for (x = bottom.getBlockX() - 1; x < top.getBlockX() + 1; x++) {
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(x, roadHeight + 1, bottom.getBlockZ() - 1);
            setWall(block, wallIds.get(ctr));
        }

        for (z = bottom.getBlockZ() - 1; z < top.getBlockZ() + 1; z++) {
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(top.getBlockX() + 1, roadHeight + 1, z);
            setWall(block, wallIds.get(ctr));
        }

        for (x = top.getBlockX() + 1; x > bottom.getBlockX() - 1; x--) {
            currentBlockId = wallIds.get(ctr);
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(x, roadHeight + 1, top.getBlockZ() + 1);
            setWall(block, currentBlockId);
        }

        for (z = top.getBlockZ() + 1; z > bottom.getBlockZ() - 1; z--) {
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(bottom.getBlockX() - 1, roadHeight + 1, z);
            setWall(block, wallIds.get(ctr));
        }
    }

    @SuppressWarnings("deprecation")
    private void setWall(IBlock block, String currentBlockId) {
        BukkitBlockRepresentation blockRep = new BukkitBlockRepresentation(currentBlockId);
        block.setTypeIdAndData(blockRep.getId(), blockRep.getData(), false);
    }

    @Override
    public Location getPlotHome(PlotId id) {
        Vector bottom = getPlotBottomLoc(id);
        Vector top = getPlotTopLoc(id);
        return new Location(world, (top.getX() + bottom.getX() + 1) / 2, getGroundHeight() + 2,
                (top.getZ() + bottom.getZ() + 1) / 2);
    }

    @Override
    public Vector getPlotMiddle(PlotId id) {
        Vector bottom = getPlotBottomLoc(id);
        Vector top = getPlotTopLoc(id);

        double x = (top.getX() + bottom.getX() + 1) / 2;
        double y = getGroundHeight() + 1;
        double z = (top.getZ() + bottom.getZ() + 1) / 2;


        return new Vector(x, y, z);
    }
}
