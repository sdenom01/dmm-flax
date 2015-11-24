package scripts;

import org.tribot.api.General;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;

@ScriptManifest(authors = {"Xuubasa, Zope, tsh"}, name = "FlaxPicker", category = "DMM-Flax", version = 1.0, description = "Picks Flax and spins it into bowstrings in seers village before noting and trading to 'mule' bot.")
public class FlaxPicker extends Script implements MessageListening07 {

    int currentStatus = 0;

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

        while(currentStatus != Constants.STATUS_QUIT) {
            switch (currentStatus) {

                case Constants.STATUS_INIT:
                    init();
                    break;

                case Constants.STATUS_GATHER_FLAX:
                    if(!Constants.seersFlaxArea.contains(Player.getRSPlayer())) {
                        walkTo(Constants.flaxTile);
                    }
                    if(gatherFlax()) {
                        currentStatus = Constants.STATUS_SPIN_FLAX;
                    }
                    break;

                case Constants.STATUS_SPIN_FLAX:
                    if(!Constants.spinningArea.contains(Player.getRSPlayer())) {
                        walkTo(Constants.spinLadderTile);
                    }
                    if(spinFlax()) {
                        currentStatus = Constants.STATUS_NOTE_BOWSTRINGS;
                    }
                    break;

                case Constants.STATUS_NOTE_BOWSTRINGS:
                    if(Constants.seersBankArea.contains(Player.getRSPlayer())) {
                        walkTo(Constants.bankTile);
                    }
                    noteBowStrings();
                    currentStatus = Constants.STATUS_TRADE_MASTER;
                    break;

                case Constants.STATUS_TRADE_MASTER:
                    if(tradeMaster()) {
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
        if(Camera.getCameraAngle() < 90) {
            Camera.setCameraAngle(90);
        }

        //Next action depends on where we are located and the contents of our inventory
        switch(locatePlayer()) {
            case Constants.bankAreaInt:
                if(Inventory.isFull()) {
                    currentStatus = Constants.STATUS_NOTE_BOWSTRINGS;
                } else {
                    //Check for noted bowstrings and trade if they exist, otherwise gather flax.
                    if(Inventory.find(Constants.notedBowStringId).length > 0) {
                        currentStatus = Constants.STATUS_TRADE_MASTER;
                    } else {
                        currentStatus = Constants.STATUS_GATHER_FLAX;
                    }
                }
                break;

            case Constants.flaxAreaInt:
                if(!Inventory.isFull()) {
                    currentStatus = Constants.STATUS_GATHER_FLAX;
                } else {
                    currentStatus = Constants.STATUS_SPIN_FLAX;
                }
                break;

            case Constants.seersVillageAreaInt:
                if(Inventory.isFull()) {
                    if(Inventory.getCount(Constants.bowStringId) != 28) {
                        currentStatus = Constants.STATUS_SPIN_FLAX;
                    } else {
                        currentStatus = Constants.STATUS_NOTE_BOWSTRINGS;
                    }
                } else {
                    currentStatus = Constants.STATUS_GATHER_FLAX;
                }
                break;

            case Constants.spinningAreaInt:
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
     * @return Area Id as integer.
     */
    public int locatePlayer() {
        if(Constants.seersBankArea.contains(Player.getRSPlayer())) {
            return Constants.bankAreaInt;
        } else if(Constants.seersFlaxArea.contains(Player.getRSPlayer())) {
            return Constants.flaxAreaInt;
        } else if(Constants.seersVillageArea.contains(Player.getRSPlayer())) {
            return Constants.seersVillageAreaInt;
        } else if(Constants.spinningArea.contains(Player.getRSPlayer())) {
            return Constants.spinningAreaInt;
        } else if(Constants.lumbridgeArea.contains(Player.getRSPlayer())) {
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
     * @return
     */
    public boolean tradeMaster() {
        return true;
    }

    /**
     * Gathers flax until inventory is full, assumes that player is located in flax area.
     * @return
     */
    public boolean gatherFlax() {
        return true;
    }

    /**
     * Spins flax in seers village until no flax remains in inventory. assumes player is in spinning area.
     * @return
     */
    public boolean spinFlax() {
        return true;
    }
}
