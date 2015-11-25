package src.scripts;

import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;

/**
 * Created by Shane on 11/24/2015.
 */
public class Constants {
    //TODO: These need actual values.
    public static final int bowStringId = -1;
    public static final int notedBowStringId = -1;


    /* Object Ids */
    public static final int spinningLadder = 25938;
    public static final int spinningWheel = 25824;


    /* Slave Status Ids */
    public static final int STATUS_INIT = 1;
    public static final int STATUS_GATHER_FLAX = 2;
    public static final int STATUS_SPIN_FLAX = 3;
    public static final int STATUS_NOTE_BOWSTRINGS = 4;
    public static final int STATUS_TRADE_MASTER = 5;
    public static final int STATUS_DEATH_RECOVER = 6;
    public static final int STATUS_QUIT = 7;

    /* Tile Ids */
    public static final RSTile flaxTile = new RSTile(2742, 3446, 0);
    public static final RSTile bankTile = new RSTile(2725, 3492, 0);
    public static final RSTile spinLadderTile = new RSTile(2715, 3471);

    /* Area Ids */
    public static final RSArea seersBankArea = new RSArea(new RSTile(2720, 3491), new RSTile(2720, 3498));
    public static final RSArea seersFlaxArea = new RSArea(new RSTile(2736, 3435), new RSTile(2752, 3454));
    public static final RSArea seersVillageArea = new RSArea(new RSTile(2662, 3511), new RSTile(2785, 3435));
    public static final RSArea lumbridgeArea = new RSArea(new RSTile(3191, 3196), new RSTile(3246, 3241));
    public static final RSArea spinningArea = new RSArea(new RSTile(2710, 3473, 1), new RSTile(2714, 3471, 1));
    public static final RSArea lowerSpinningArea = new RSArea(new RSTile(2710, 3473, 0), new RSTile(2714, 3471, 0));

    /* Corresponding integer identifiers for Areas */
    public static final int bankAreaInt = 1;
    public static final int flaxAreaInt = 2;
    public static final int spinningAreaInt = 3;
    public static final int seersVillageAreaInt = 4;
    public static final int lumbridgeAreaInt = 5;
    public static final int lowerSpinningAreaInt = 6;
}
