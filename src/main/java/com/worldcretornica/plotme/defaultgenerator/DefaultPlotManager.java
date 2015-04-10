package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FILL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FOR_SALE_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_FLOOR_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PROTECTED_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.UNCLAIMED_WALL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.WALL_BLOCK;

import com.worldcretornica.plotme.defaultgenerator.bukkit.BukkitDefaultGenerator;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import com.worldcretornica.plotme_core.PlotId;
import com.worldcretornica.plotme_core.api.IBlock;
import com.worldcretornica.plotme_core.api.ILocation;
import com.worldcretornica.plotme_core.api.IWorld;
import com.worldcretornica.plotme_core.bukkit.api.BukkitWorld;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class DefaultPlotManager extends BukkitAbstractGenManager {

    public DefaultPlotManager(BukkitDefaultGenerator instance, ConfigurationSection wgc) {
        super(instance, wgc);
    }

    @Override
    public PlotId getPlotId(ILocation loc) {
        int posx = loc.getBlockX();
        int posz = loc.getBlockZ();
        int pathSize = wgc.getInt(PATH_WIDTH.key());
        int size = getPlotSize() + pathSize;

        return internalgetPlotId(pathSize, size, posx, posz, loc.getWorld());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillRoad(PlotId id1, PlotId id2) {
        IWorld world = id1.getWorld();
        ILocation bottomPlot1 = getPlotBottomLoc(id1);
        ILocation topPlot1 = getPlotTopLoc(id1);
        ILocation bottomPlot2 = getPlotBottomLoc(id2);
        ILocation topPlot2 = getPlotTopLoc(id2);

        int minX;
        int maxX;
        int minZ;
        int maxZ;

        int h = getGroundHeight();
        int wallId = BukkitBlockRepresentation.getBlockId(wgc.getString(UNCLAIMED_WALL.key(), "44:7"));
        byte wallValue = BukkitBlockRepresentation.getBlockData(wgc.getString(UNCLAIMED_WALL.key(), "44:7"));
        int fillId = BukkitBlockRepresentation.getBlockId(wgc.getString(FILL_BLOCK.key(), "3"));
        byte fillValue = BukkitBlockRepresentation.getBlockData(wgc.getString(FILL_BLOCK.key(), "3"));

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
                            world.getBlockAt(x, y, z).setTypeIdAndData((short) wallId, wallValue, false);
                        } else {
                            world.getBlockAt(x, y, z).setType(Material.AIR, false);
                        }
                    } else {
                        world.getBlockAt(x, y, z).setTypeIdAndData((short) fillId, fillValue, false);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillMiddleRoad(PlotId id1, PlotId id2) {
        World world = ((BukkitWorld) id1.getWorld()).getWorld();
        ILocation bottomPlot1 = getPlotBottomLoc(id1);
        ILocation topPlot1 = getPlotTopLoc(id1);
        ILocation bottomPlot2 = getPlotBottomLoc(id2);
        ILocation topPlot2 = getPlotTopLoc(id2);

        int height = getGroundHeight();
        int fillId = BukkitBlockRepresentation.getBlockId(wgc.getString(PLOT_FLOOR_BLOCK.key()));

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
        ILocation pillar = new ILocation(id.getWorld(), bottomX(id) - 1, getGroundHeight() + 1, bottomZ(id) - 1);
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

        ILocation pillar = new ILocation(id.getWorld(), bottomX(id) - 1, getGroundHeight() + 1, bottomZ(id) - 1);

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
        IWorld world = id.getWorld();
        ILocation bottom = getPlotBottomLoc(id);

        ILocation pillar = new ILocation(world, bottom.getX() - 1, getGroundHeight() + 1, bottom.getZ() - 1);

        IBlock bsign = pillar.add(0, 0, -1).getBlock();
        bsign.setType(Material.AIR, false);
    }

    @Override
    public void removeSellerDisplay(PlotId id) {
        ILocation bottom = getPlotBottomLoc(id);

        IWorld w = id.getWorld();
        ILocation pillar = new ILocation(w, bottom.getX() - 1, getGroundHeight() + 1, bottom.getZ() - 1);

        IBlock bsign = pillar.add(-1, 0, 0).getBlock();
        bsign.setType(Material.AIR, false);

    }
    @Override
    public ILocation getPlotBottomLoc(PlotId id) {
        int px = id.getX();
        int pz = id.getZ();

        int pathWidth = wgc.getInt(PATH_WIDTH.key());

        int x = (px * (getPlotSize() + pathWidth)) - (getPlotSize()) - ((int) Math.floor(pathWidth / 2));
        int z = pz * (getPlotSize() + pathWidth) - (getPlotSize()) - ((int) Math.floor(pathWidth / 2));

        return new ILocation(id.getWorld(), x, 0, z);
    }

    @Override
    public ILocation getPlotTopLoc(PlotId id) {
        int px = id.getX();
        int pz = id.getZ();

        int pathWidth = wgc.getInt(PATH_WIDTH.key());

        int x = px * (getPlotSize() + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;
        int z = pz * (getPlotSize() + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;

        return new ILocation(id.getWorld(), x, 256, z);
    }

    @Override
    public Long[] clear(ILocation bottom, ILocation top, long maxBlocks, Long[] start) {
        clearEntities(bottom, top);

        int roadHeight = getGroundHeight();
        BukkitBlockRepresentation fillBlock = new BukkitBlockRepresentation(wgc.getString(FILL_BLOCK.key()));
        BukkitBlockRepresentation floorBlock = new BukkitBlockRepresentation(wgc.getString(PLOT_FLOOR_BLOCK.key()));

        int bottomX;
        int topX = top.getBlockX();
        int bottomZ;
        int topZ = top.getBlockZ();

        long nbBlockClearedBefore = 0;

        IWorld world = bottom.getWorld();

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
                IBlock block = world.getBlockAt(x, 0, z);
                if (!block.getType().equals(Material.BEDROCK)) {
                    block.setType(Material.BEDROCK, false);
                }
                block.setBiome(Biome.PLAINS);

                for (int y = 1; y < 256; y++) {
                    block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.BEACON
                            || block.getType() == Material.CHEST
                            || block.getType() == Material.BREWING_STAND
                            || block.getType() == Material.DISPENSER
                            || block.getType() == Material.FURNACE
                            || block.getType() == Material.DROPPER
                            || block.getType() == Material.TRAPPED_CHEST
                            || block.getType() == Material.HOPPER
                            || block.getType() == Material.STORAGE_MINECART) {
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
                        return new Long[]{(long) x, (long) y, (long) z, nbBlockClearedBefore + nbBlockCleared};
                    }
                }
            }
            bottomZ = bottom.getBlockZ();
        }

        refreshPlotChunks(getPlotId(bottom));
        return null;
    }

    @Override
    public void adjustPlotFor(PlotId id, boolean claimed, boolean protect, boolean forSale) {
        List<String> wallIds = new ArrayList<>();
        IWorld world = id.getWorld();
        int roadHeight = getGroundHeight();

        String claimedId = wgc.getString(WALL_BLOCK.key());
        String wallId = wgc.getString(UNCLAIMED_WALL.key());
        String protectedWallId = wgc.getString(PROTECTED_WALL_BLOCK.key());
        String forsaleWallId = wgc.getString(FOR_SALE_WALL_BLOCK.key());

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

        ILocation bottom = getPlotBottomLoc(id);
        ILocation top = getPlotTopLoc(id);

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
    public ILocation getPlotHome(PlotId id) {
        ILocation bottom = getPlotBottomLoc(id);
        ILocation top = getPlotTopLoc(id);
        return new ILocation(id.getWorld(), (top.getX() + bottom.getX() + 1) / 2, getGroundHeight() + 2,
                (top.getZ() + bottom.getZ() + 1) / 2);
    }

    @Override
    public ILocation getPlotMiddle(PlotId id) {
        ILocation bottom = getPlotBottomLoc(id);
        ILocation top = getPlotTopLoc(id);

        double x = (top.getX() + bottom.getX() + 1) / 2;
        double y = getGroundHeight() + 1;
        double z = (top.getZ() + bottom.getZ() + 1) / 2;


        return new ILocation(id.getWorld(), x, y, z);
    }
}
