package scripts;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.Trading;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.util.ThreadSettings;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;

@ScriptManifest(authors = {"Xuubasa, Zope, tsh"}, name = "FlaxMaster", category = "DMM-Flax", version = 1.0, description = "Picks Flax and spins it into bowstrings in seers village before noting and trading to 'mule' bot.")


public class FlaxMaster extends Script implements MessageListening07 {

    public boolean receivedTrade = false;
    public static String foods[] = {"Trout","Salmon","Lobster","Monkfish","Shark","Cooked karambwan"};

    @Override
    public void run() {
        General.useAntiBanCompliance(true);
        ThreadSettings.get().getClickingAPIUseDynamic();
        while(true){
            sleep(50,100);
            if(beingAttacked() && (Skills.getCurrentLevel(SKILLS.HITPOINTS) != Skills.getActualLevel(SKILLS.HITPOINTS))){
                eatFood();
            }
        }
    }

    public static void eatFood(){
        RSItem food[] = Inventory.find(foods);
        final int initialHealth = Skills.getCurrentLevel(SKILLS.HITPOINTS);
        if(food.length > 0){
            if(Clicking.click(food[0])){
                Timing.waitCondition(new Condition()
                {
                    @Override
                    public boolean active()
                    {
                        return Skills.getCurrentLevel(SKILLS.HITPOINTS) > initialHealth;
                    }
                }, General.random(1000, 2000));
            }
        }
    }

    public boolean beingAttacked(){
        return Player.getRSPlayer().isInCombat();
    }

    public void tradeRequestReceived(String arg0) {
        RSPlayer[] trader = Players.find(arg0);
        if(trader.length > 0){
            if(Clicking.click("Trade with",trader)){
                Timing.waitCondition(new Condition()
                {
                    @Override
                    public boolean active()
                    {
                        General.sleep(500,1000);
                        return Trading.getWindowState() == Trading.WINDOW_STATE.FIRST_WINDOW;
                    }
                }, General.random(1000, 2000));
                if(Trading.hasAccepted(true)){
                    if(Trading.accept()){
                        Timing.waitCondition(new Condition()
                        {
                            @Override
                            public boolean active()
                            {
                                General.sleep(500);
                                return Trading.getWindowState() == Trading.WINDOW_STATE.SECOND_WINDOW;
                            }
                        }, General.random(1000, 2000));
                        if(Trading.accept()){
                            println("Trade accepted!");
                        }
                    }
                }
            }
        }
    }

    public void serverMessageReceived(String arg0) {
        if(arg0.contains("Other player")){
            println("Other player declined! Waiting for timeout...");
        }
    }

    public void clanMessageReceived(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void duelRequestReceived(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void personalMessageReceived(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void playerMessageReceived(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }
}