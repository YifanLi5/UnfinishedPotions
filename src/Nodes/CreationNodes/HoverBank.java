package Nodes.CreationNodes;

import Nodes.ExecutableNode;
import ScriptClasses.HerbEnum;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import static ScriptClasses.Statics.*;

public class HoverBank extends AbstractCreationNode{

    public static final int NODE_EXECUTION_WEIGHT = 20;
    private RS2Object grandExchangeBooth;
    private static final int GRAND_EXCHANGE_BOOTH_ID = 10060;
    private static ExecutableNode singleton;

    private HoverBank(Script hostScriptRefence, HerbEnum herbEnum) {
        super(hostScriptRefence, herbEnum);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence, HerbEnum herbEnum){
        if(singleton == null) singleton = new HoverBank(hostScriptRefence, herbEnum);
        return singleton;
    }

    @Override
    int waitForPotions() {
        Inventory inv = hostScriptReference.getInventory();
        //wait until all potions are finished
        if(grandExchangeBooth == null || !grandExchangeBooth.exists()) grandExchangeBooth = findGrandExchangeBooth();
        grandExchangeBooth.hover();
        new ConditionalSleep(10000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(VIAL_OF_WATER) || !inv.contains(cleanHerb.getItemID());
            }
        }.sleep();


        return (int) Statics.randomNormalDist(1000, 300);
    }

    private RS2Object findGrandExchangeBooth(){
        RS2Object grandExchangeBooth = hostScriptReference.getObjects().closest(GRAND_EXCHANGE_BOOTH_ID);
        if(grandExchangeBooth != null){
            if(!grandExchangeBooth.isVisible()){
                hostScriptReference.camera.toEntity(grandExchangeBooth);
            }
            return grandExchangeBooth;
        }
        return null;

    }

    @Override
    public int getDefaultEdgeWeight() {
        return NODE_EXECUTION_WEIGHT;
    }

}
