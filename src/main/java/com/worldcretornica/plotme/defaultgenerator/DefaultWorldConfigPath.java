package com.worldcretornica.plotme.defaultgenerator;

import com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath;
import com.worldcretornica.plotme_abstractgenerator.WorldConfigPath;

public enum DefaultWorldConfigPath implements WorldConfigPath {

    PLOT_SIZE(AbstractWorldConfigPath.PLOT_SIZE),
    GROUND_LEVEL(AbstractWorldConfigPath.GROUND_LEVEL),
    FILL_BLOCK(AbstractWorldConfigPath.FILL_BLOCK),
    PATH_WIDTH("PathWidth", 7),
    PLOT_FLOOR_BLOCK("PlotFloorBlock", "2"),
    ROAD_MAIN_BLOCK("RoadMainBlock", "5"),
    ROAD_ALT_BLOCK("RoadAltBlock", "5:2"),
    WALL_BLOCK("WallBlock", "44"),
    PROTECTED_WALL_BLOCK("ProtectedWallBlock", "44:4"),
    FOR_SALE_WALL_BLOCK("ForSaleWallBlock", "44:1"),
    UNCLAIMED_WALL("UnclaimedBorder", "44:7");

    private final String key;
    private final Object def;

    DefaultWorldConfigPath(String key, Object def) {
        this.key = key;
        this.def = def;
    }

    DefaultWorldConfigPath(AbstractWorldConfigPath awcp) {
        this.key = awcp.key();
        this.def = awcp.value();
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Object value() {
        return def;
    }
}
