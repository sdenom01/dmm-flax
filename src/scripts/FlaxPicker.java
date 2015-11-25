package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.util.PathNavigator;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;

import java.awt.*;

@ScriptManifest(authors = {"Xuubasa, Zope, tsh"}, name = "FlaxPicker", category = "DMM-Flax", version = 1.0, description = "Picks Flax and spins it into bowstrings in seers village before noting and trading to 'mule' bot.")
public class FlaxPicker extends Script implements MessageListening07 {

    int currentStatus = 1;

    @Override
    public void serverMessageReceived(String s) {

    }

    @Override
    public void tradeRequestReceived(String s) {

    }

    @Override
    public void duelRequestReceived(String s, String s1) {

    }

    @Override
    public void clanMessageReceived(String s, String s1) {

    }

    @Override
    public void playerMessageReceived(String s, String s1) {

    }

    @Override
    public void personalMessageReceived(String s, String s1) {

    }

    @Override
    public void run() {

        while (currentStatus != Constants.STATUS_QUIT) {
            println("Current Status: " + currentStatus);
            switch (currentStatus) {

                case Constants.STATUS_INIT:
                    init();
                    break;

                case Constants.STATUS_GATHER_FLAX:
                    if (!Constants.seersFlaxArea.contains(Player.getRSPlayer())) {
                        walkTo(Constants.flaxTile);
                    }
                    if (gatherFlax()) {
                        currentStatus = Constants.STATUS_SPIN_FLAX;
                    }
                    break;

                case Constants.STATUS_SPIN_FLAX:
                    PathNavigator navi = new PathNavigator();
                    if(!Constants.spinningArea.contains(Player.getRSPlayer())) {
                        navi.traverse(Constants.spinLadderTile);
                        useLadder(Constants.spinningLadder, "Climb-up", Constants.spinningArea);
                    }

                    if(spinFlax()) {
                        useLadder(Constants.spinningLadder, "Climb-down", Constants.lowerSpinningArea);
                        navi.traverse(Constants.bankTile);
                        currentStatus = Constants.STATUS_NOTE_BOWSTRINGS;
                    }
                    break;

                case Constants.STATUS_NOTE_BOWSTRINGS:
                    if (Constants.seersBankArea.contains(Player.getRSPlayer())) {
                        walkTo(Constants.bankTile);
                    }
                    noteBowStrings();
                    currentStatus = Constants.STATUS_TRADE_MASTER;
                    break;

                case Constants.STATUS_TRADE_MASTER:
                    if (tradeMaster()) {
                        currentStatus = Constants.STATUS_GATHER_FLAX;
                    }
                    break;

                case Constants.STATUS_DEATH_RECOVER:
                    walkTo(Constants.flaxTile);
                    currentStatus = Constants.STATUS_GATHER_FLAX;
                    break;

            }
            General.sleep(100);
        }

    }

    /**
     * initializes the flax picker and sets the status to the next required action.
     */
    public void init() {
        if (Camera.getCameraAngle() < 90) {
            Camera.setCameraAngle(90);
        }

        //Next action depends on where we are located and the contents of our inventory
        switch (locatePlayer()) {
            case Constants.bankAreaInt:
                if (Inventory.isFull()) {
                    currentStatus = Constants.STATUS_NOTE_BOWSTRINGS;
                } else {
                    //Check for noted bowstrings and trade if they exist, otherwise gather flax.
                    if (Inventory.find(Constants.notedBowStringId).length > 0) {
                        currentStatus = Constants.STATUS_TRADE_MASTER;
                    } else {
                        currentStatus = Constants.STATUS_GATHER_FLAX;
                    }
                }
                break;

            case Constants.flaxAreaInt:
                if (!Inventory.isFull()) {
                    currentStatus = Constants.STATUS_GATHER_FLAX;
                } else {
                    currentStatus = Constants.STATUS_SPIN_FLAX;
                }
                break;

            case Constants.seersVillageAreaInt:
                if (Inventory.isFull()) {
                    if (Inventory.getCount(Constants.bowStringId) != 28) {
                        currentStatus = Constants.STATUS_SPIN_FLAX;
                    } else {
                        currentStatus = Constants.STATUS_NOTE_BOWSTRINGS;
                    }
                } else {
                    currentStatus = Constants.STATUS_GATHER_FLAX;
                }
                break;

            case Constants.spinningAreaInt:
            case Constants.lowerSpinningAreaInt:
                if(Inventory.getCount(Constants.bowStringId) != 28) {
                    currentStatus = Constants.STATUS_SPIN_FLAX;
                } else {
                    currentStatus = Constants.STATUS_NOTE_BOWSTRINGS;
                }
                break;

            case Constants.lumbridgeAreaInt:
                General.println("Player found in Lumbridge, starting death recovery.");
                currentStatus = Constants.STATUS_DEATH_RECOVER;
                break;

            case -1:
                General.println("Player not in a recognized area.");
                currentStatus = Constants.STATUS_DEATH_RECOVER;
                break;
        }
    }

    /**
     * Checks all known areas for the player and returns its corresponding area integer Id.
     *
     * @return Area Id as integer.
     */
    public int locatePlayer() {
        if (Constants.seersBankArea.contains(Player.getRSPlayer())) {
            return Constants.bankAreaInt;
        } else if (Constants.seersFlaxArea.contains(Player.getRSPlayer())) {
            return Constants.flaxAreaInt;
        } else if (Constants.seersVillageArea.contains(Player.getRSPlayer())) {
            return Constants.seersVillageAreaInt;
        } else if (Constants.spinningArea.contains(Player.getRSPlayer())) {
            return Constants.spinningAreaInt;
        } else if (Constants.lumbridgeArea.contains(Player.getRSPlayer())) {
            return Constants.lumbridgeAreaInt;
        }
        return -1;
    }

    public boolean walkTo(RSTile tile) {
        if (!WebWalking.getUseRun()) {
            WebWalking.setUseRun(true);
        } else {
            return WebWalking.walkTo(tile);
        }
        return false;
    }

    /**
     * Notes bowstrings in inventory, assumes player is located near a bank.
     */
    public void noteBowStrings() {

    }

    /**
     * Sends trade request to master and trades noted bowstrings. Returns false if trade fails
     *
     * @return
     */
    public boolean tradeMaster() {
        return true;
    }

    /**
     * Gathers flax until inventory is full, assumes that player is located in flax area.
     *
     * @return
     */
    public boolean gatherFlax() {
        if (Inventory.find(Constants.flaxId).length == 28)
            return true;
        else {
            final RSObject[] flaxes = Objects.findNearest(50, "Flax");
            if (flaxes.length < 1)
                return false;

            if (!flaxes[0].isOnScreen()) {
                // The nearest flax is not on the screen. Let's walk to it.

                if (!Walking.walkPath(Walking.generateStraightPath(flaxes[0])))
                    // We could not walk to the flax. Let's exit so we don't try
                    // clicking a flax which isn't on screen.
                    return false;

                if (!Timing.waitCondition(new Condition() {
                    // We will now use the Timing API to wait until the flax is on
                    // the screen (we are probably walking to the flax right now).

                    @Override
                    public boolean active() {
                        General.sleep(100); // Sleep to reduce CPU usage.

                        return flaxes[0].isOnScreen();
                    }

                }, General.random(8000, 9300)))
                    // A flax could not be found before the timeout of 8-9.3
                    // seconds. Let's exit the method and return false. we don't
                    // want to end up trying to click a flax which isn't on the
                    // screen.
                    return false;
            }

            // Okay, now we are sure flaxes[0] is on-screen. Let's click it. We may
            // be still moving at this moment, so let's use DynamicClicking.
            // DynamicClicking should be used when your character is moving, or the
            // target is moving, and you need to click the target.

            if (!DynamicClicking.clickRSObject(flaxes[0], "Pick"))
                return false;
            else
                sleep(750, 1250);
        }

        return false;
    }

    /**
     * Spins flax in seers village until no flax remains in inventory. assumes player is in spinning area.
     *
     * @return
     */
    public boolean spinFlax() {
        RSObject[] spinningWheel = Objects.findNearest(20, Constants.spinningWheel);
        if(spinningWheel.length > 0) {
            DynamicClicking.clickRSModel(spinningWheel[0].getModel(), "Spin");
            //Window Pops up
            if(clickObject(spinningWheel[0], "Spin", 3, 10)){
                long t = System.currentTimeMillis();
                while((Interfaces.get(459, 91) == null || Interfaces.get(459, 91).getParentID() != -1) && Timing.timeFromMark(t) < 2000){
                    sleep(10);
                }
            }

        } else {
            General.println("Could not find spinning wheel.");
            return false;
        }

        return true;
    }


    public static void useLadder(int ladderID, String action, final RSArea endArea) {
        RSObject[] ladderObject = Objects.findNearest(20, ladderID);
        //While found ladder
        while (ladderObject.length > 0) {
            General.println("Found Ladder: " + ladderID + ", Attempting to " + action);
            DynamicClicking.clickRSModel(ladderObject[0].getModel(), action);
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return endArea.contains(Player.getRSPlayer());
                }
            }, 2000);
            ladderObject = Objects.findNearest(20, ladderID);
        }
        General.println("Finished ladder interaction.");
    }

    private boolean clickObject(RSObject object, String option, int randomx, int randomy){
        try{
            if(object != null && object.isOnScreen() && object.getModel() != null){
                Point x = object.getModel().getCentrePoint();
                int x1 = (int) x.getX() + General.random(randomx, -randomx);
                int x2 = (int) x.getY() + General.random(randomy, -randomy);
                int tries = 0;
                while((int) Mouse.getPos().getX() != x1 && (int)Mouse.getPos().getY() != x2 && tries < 30) {
                    Mouse.move(x1, x2);
                    tries++;
                }
                Mouse.click(x1, x2, 3);
                if(ChooseOption.isOpen()){
                    if(ChooseOption.isOptionValid(option)){
                        ChooseOption.select(option);
                        return true;
                    } else {
                        Mouse.move((int)Mouse.getPos().getX() + General.random(15, 25),(int) Mouse.getPos().getY() + General.random(-60, -80));
                    }
                }
            }
        } catch (NullPointerException e){
            System.out.print("Your computer sux, recovered from nullpointer");
        }
        return false;
    }
}
