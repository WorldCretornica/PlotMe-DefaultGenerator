package com.worldcretornica.plotme.defaultgenerator;

import com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath;
import com.worldcretornica.plotme_abstractgenerator.WorldConfigPath;

/**
 *
 * @author Fabrizio Lungo <fab@lungo.co.uk>
 */
public enum DefaultWorldConfigPath implements WorldConfigPath {

    PLOT_SIZE(AbstractWorldConfigPath.PLOT_SIZE),
    X_TRANSLATION(AbstractWorldConfigPath.X_TRANSLATION),
    Z_TRANSLATION(AbstractWorldConfigPath.Z_TRANSLATION),
    GROUND_LEVEL(AbstractWorldConfigPath.GROUND_LEVEL),
    FILL_BLOCK(AbstractWorldConfigPath.FILL_BLOCK),
    PATH_WIDTH("PathWidth", 7),
    PLOT_FLOOR_BLOCK("PlotFloorBlock", "2"),
    ROAD_MAIN_BLOCK("RoadMainBlock", "5"),
    ROAD_ALT_BLOCK("RoadAltBlock", "5:2"),
    WALL_BLOCK("WallBlock", "44"),
    PROTECTED_WALL_BLOCK("ProtectedWallBlock", "44:4"),
    AUCTION_WALL_BLOCK("AuctionWallBlock", "44:1"),
    FOR_SALE_WALL_BLOCK("ForSaleWallBlock", "44:1");

    public final String path;
    public final Object def;

    DefaultWorldConfigPath(String path, Object def) {
        this.path = path;
        this.def = def;
    }

    DefaultWorldConfigPath(AbstractWorldConfigPath awcp) {
        this.path = awcp.path;
        this.def = awcp.def;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Object def() {
        return def;
    }
}
