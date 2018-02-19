package Nodes.CreationNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Statics;
import org.osbot.rs07.api.EntityAPI;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.Statics.*;

public class HoverBank extends AbstractCreationNode{

    private RS2Object grandExchangeBooth;
    private static final int GRAND_EXCHANGE_BOOTH_ID = 10060;
    private static ExecutableNode singleton;

    private HoverBank(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    @Override
    int waitForPotions() {
        Inventory inv = hostScriptRefence.getInventory();
        //wait until all potions are finished
        if(grandExchangeBooth == null || !grandExchangeBooth.exists()){
            grandExchangeBooth = findGrandExchangeBooth();
        }
        grandExchangeBooth.hover();
        new ConditionalSleep(10000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(VIAL_OF_WATER) || !inv.contains(CLEAN_HERB);
            }
        }.sleep();


        return (int) Statics.randomNormalDist(1000, 300);
    }

    private RS2Object findGrandExchangeBooth(){
        RS2Object grandExchangeBooth = hostScriptRefence.getObjects().closest(GRAND_EXCHANGE_BOOTH_ID);
        if(grandExchangeBooth != null){
            if(!grandExchangeBooth.isVisible()){
                hostScriptRefence.camera.toEntity(grandExchangeBooth);
            }
            return grandExchangeBooth;
        }
        return null;

    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new HoverBank(hostScriptRefence);
        }
        return singleton;
    }



}
