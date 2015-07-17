package com.worldcretornica.plotme.defaultgenerator;

import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotId;
import com.worldcretornica.plotme_core.api.IBlock;
import com.worldcretornica.plotme_core.api.IWorld;
import com.worldcretornica.plotme_core.api.Location;
import com.worldcretornica.plotme_core.api.Vector;
import com.worldcretornica.plotme_core.bukkit.api.BukkitBlock;
import com.worldcretornica.plotme_core.bukkit.api.BukkitWorld;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        int x = px * (getPlotSize() + pathWidth) - (getPlotSize()) - (int) Math.floor(pathWidth / 2);
        int z = pz * (getPlotSize() + pathWidth) - (getPlotSize()) - (int) Math.floor(pathWidth / 2);

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
    public void clear(Vector bottom, Vector top) {
        clearEntities(bottom, top);
        IBlock[] materials = new IBlock[65536];
        Set<ChunkCoords> chunks = new HashSet<>();

        for (int x = bottom.getBlockX(); x <= top.getBlockX(); ++x) {
            for (int z = bottom.getBlockZ(); z <= top.getBlockZ(); ++z) {
                chunks.add(new ChunkCoords(x >> 4, z >> 4));
            }
        }
        for (ChunkCoords chunk : chunks) {
            Vector min = new Vector(chunk.getX() << 4, 0, chunk.getZ() << 4);
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 256; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        materials[index] = world.getBlockAt(pt);
                    }
                }
            }
            ((BukkitWorld) world).getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 256; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 256 + z * 16 + x;
                        int lowestX = Math.min(bottom.getBlockX(), top.getBlockX());
                        int highestX = Math.max(bottom.getBlockX(), top.getBlockX());
                        int lowestZ = Math.min(bottom.getBlockZ(), top.getBlockZ());
                        int highestZ = Math.max(bottom.getBlockZ(), top.getBlockZ());

                        boolean contains =
                                pt.getBlockX() >= lowestX && pt.getBlockX() <= highestX && pt.getBlockZ() >= lowestZ && pt.getBlockZ() <= highestZ;
                        if (!contains) {
                            BukkitBlock block = ((BukkitBlock) materials[index]);
                            BukkitBlock blockAt = (BukkitBlock) world.getBlockAt(pt);
                            blockAt.setTypeIdAndData((short) block.getTypeId(), block.getData(), false);
                            if (block.getState() instanceof InventoryHolder) {
                                if (blockAt.getState() instanceof InventoryHolder) {
                                    ((InventoryHolder) blockAt.getState()).getInventory()
                                            .setContents(((InventoryHolder) block.getState()).getInventory().getContents());
                                }
                            }
                        }
                    }
                }
            }
            world.refreshChunk(chunk.getX(), chunk.getZ());
        }
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
        return new Location(world, bottom.getX() + ((top.getX() - bottom.getX()) / 2), getGroundHeight() + 2,
                bottom.getZ() - 2);
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

    private class ChunkCoords {

        private final int x;
        private final int z;

        public ChunkCoords(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getZ() {
            return z;
        }

        public int getX() {
            return x;
        }
    }
}
