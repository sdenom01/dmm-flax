package scripts;

import com.sun.tools.internal.jxc.ap.Const;
import org.tribot.api.Clicking;
import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.api2007.util.PathNavigator;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;
import scripts.*;

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
                    if (!Constants.spinningArea.contains(Player.getRSPlayer())) {
                        walkTo(Constants.spinningDoorTile);
                        openDoorIfClosed(Objects.findNearest(20, Constants.spinningDoor));
                        walkTo(Constants.spinLadderTile);
                        useLadder(Constants.spinningLadderBottom, "Climb-up", Constants.spinningArea);
                    } else {
                        println("In spinning area...");
                    }

                    if (spinFlax()) {
                        useLadder(Constants.spinningLadderTop, "Climb-down", Constants.lowerSpinningArea);
                        openDoorIfClosed(Objects.findNearest(20, Constants.spinningDoor));
                        walkTo(Constants.bankTile);
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
                        General.println("finished trading master");
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
                if (Inventory.getCount(Constants.bowStringId) != 28) {
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
    public boolean noteBowStrings() {
        General.println("noting bowstrings");
        RSItem[] bowStrings = Inventory.find(Constants.bowStringId);

//        if(locatePlayer() == 1 && bowStrings.length > 0) {
//            General.println("we're in seers bank");
            // exchange bowstrings
            RSObject[] nearestBankBooth = Objects.findNearest(15, 25808);
            withdrawBowstrings(nearestBankBooth);
//        } else if(bowStrings.length == 0) {
//            // run to flax fields
//            walkTo(Constants.flaxTile);
//        } else if(locatePlayer() != 1) {
//            // run to bank
//            walkTo(Constants.bankTile);
//        }
        RSItem[] notedBowStrings = Inventory.find(Constants.notedBowStringId);
        if (notedBowStrings.length == 0) {
            withdrawBowstrings(nearestBankBooth);
            return false;
        } else {
            return true;
        }
    }


    /**
     * Withdraws noted bowstrings from bank.
     */
    public boolean withdrawBowstrings(RSObject[] bankBooth) {
        General.println("withdrawBowstrings");
        Clicking.click("Bank Bank booth", bankBooth[0]);
        General.random(500, 800);
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                return Banking.isBankScreenOpen();
            }
        }, 5000);

        General.random(500, 800);
        if (Inventory.find(Constants.bowStringId).length > 0) {
            Banking.depositAll();
            General.sleep(400, 500);
        }

        RSItem[] notedBowStrings = Inventory.find(Constants.notedBowStringId);
        if (notedBowStrings.length == 0) {
            // note items
            Clicking.click(Interfaces.get(12, 24));
            General.sleep(400, 500);
            Banking.withdraw(0, Constants.bowStringId);
            return true;
        }

        General.sleep(400, 500);
        if (notedBowStrings.length > 1) {
            Banking.close();
            General.sleep(400, 500);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sends trade request to master and trades noted bowstrings. Returns false if trade fails
     *
     * @return
     */
    public boolean tradeMaster() {
        General.println("trade master");
        if (Banking.isBankScreenOpen()) {
            Banking.close();
        }
        if (Inventory.find(Constants.bowStringId).length > 0) {
            noteBowStrings();
        }

        // define mule names
        String accountNames = "zsh";
        RSPlayer[] mule = Players.findNearest(accountNames);
        RSPlayer[] all = Players.getAll();
        RSItem[] notedBowStrings = Inventory.find(Constants.notedBowStringId);
        boolean tradeComplete = false;
        if (all.length > 0) {
            for (RSPlayer r : all) {
                if (r != null) {
                    if (r.getDefinition() != null) {
                        if (accountNames.contains(r.getName())) {
                            if (r.isOnScreen()) {
                                Clicking.click("Trade with " + accountNames, mule);
                                General.sleep(1000,5000);

                                if (Trading.getWindowState() == Trading.WINDOW_STATE.FIRST_WINDOW && accountNames.contains(Trading.getOpponentName())) {
                                    Clicking.click("Offer-All", notedBowStrings);
                                    //1st screen
                                    Trading.accept();
                                }
                                if (Trading.getWindowState() == Trading.WINDOW_STATE.SECOND_WINDOW && accountNames.contains(Trading.getOpponentName())) {
                                    Trading.accept();
                                    General.sleep(1000,5000);
                                    tradeComplete = true;
                                }
                            } else {
                                if (Player.getPosition().distanceTo(r) > 5) {
                                    WebWalking.walkTo(r);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (tradeComplete) { return true; } else { return false; }
    }

    /**
     * Gathers flax until inventory is full, assumes that player is located in flax area.
     *
     * @return
     */
    public boolean gatherFlax() {
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

        if (!Inventory.isFull()) {
            if (!Clicking.click("Pick", flaxes[0]))
                return false;
            else
                sleep(750, 1250);
        } else {
            return true;
        }
        return false;
    }

    /**
     * Spins flax in seers village until no flax remains in inventory. assumes player is in spinning area.
     *
     * @return
     */
    public boolean spinFlax() {
        println("Starting to spin flax...");
        RSObject[] spinningWheel = Objects.findNearest(20, Constants.spinningWheel);
        if (spinningWheel.length > 0) {
//            Clicking.click("Spin", spinningWheel[0].getModel());
//            sleep(500, 750);

            //Window Pops up
            if (spinningWheel[0].click("Spin")) {

                final RSInterface[] bowStringInterface = {null};
                final RSInterface[] amountInterface = {null};

                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        println("Waiting for clickable interface..");
                        sleep(50);
                        bowStringInterface[0] = Interfaces.get(459, 92);
                        return bowStringInterface[0] != null;
                    }
                }, 5000);

                sleep(500, 750);

                if (bowStringInterface[0] != null)
                    Clicking.click("Make X", bowStringInterface[0]);
                General.random(500, 800);
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        println("Waiting for input interface..");
                        sleep(50);
                        amountInterface[0] = Interfaces.get(162, 32);
                        return amountInterface[0] != null;
                    }
                }, 5000);

                sleep(950, 1250);

                Keyboard.typeSend("" + General.random(28, 99));
                sleep(750, 1250);

            }

            int flaxLeft = Inventory.getCount(Constants.flaxId);
            while (flaxLeft > 0) {
                if (isCurrentlySpinning()) {
                    General.println(flaxLeft + " more flax to spin");
                    sleep(50);
                } else {
                    println("Not spinning!");
                    return false;
                }

                flaxLeft = Inventory.getCount(Constants.flaxId);
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

    private boolean clickObject(RSObject object, String option, int randomx, int randomy) {
        try {
            if (object != null && object.isOnScreen() && object.getModel() != null) {
                Point x = object.getModel().getCentrePoint();
                int x1 = (int) x.getX() + General.random(randomx, -randomx);
                int x2 = (int) x.getY() + General.random(randomy, -randomy);
                int tries = 0;
                while ((int) Mouse.getPos().getX() != x1 && (int) Mouse.getPos().getY() != x2 && tries < 30) {
                    Mouse.move(x1, x2);
                    tries++;
                }
                Mouse.click(x1, x2, 3);
                if (ChooseOption.isOpen()) {
                    if (ChooseOption.isOptionValid(option)) {
                        ChooseOption.select(option);
                        return true;
                    } else {
                        Mouse.move((int) Mouse.getPos().getX() + General.random(15, 25), (int) Mouse.getPos().getY() + General.random(-60, -80));
                    }
                }
            }
        } catch (NullPointerException e) {
            System.out.print("Your computer sux, recovered from nullpointer");
        }
        return false;
    }

    /**
     * This method compares the players starting crafting experience and keeps track of it to help us know if we are charging
     */
    private boolean isCurrentlySpinning() {
        int startXp = Skills.getXP(Skills.SKILLS.CRAFTING);
        long t = System.currentTimeMillis();
        // Wait for 3 seconds before timing out...
        while (Timing.timeFromMark(t) < 4250) {
            if (Skills.getXP(Skills.SKILLS.CRAFTING) > startXp) {
                // If we gained exp...
                return true;
            }

            sleep(100);
        }

        return false;
    }

    public void openDoorIfClosed(int doorId) {
        RSObject[] door = Objects.findNearest(20, doorId);
        while(door.length > 0) {
            DynamicClicking.clickRSModel(door[0].getModel(), "Open");
            sleep(200, 300);
            door = Objects.findNearest(20, doorId);
        }
    }

}
