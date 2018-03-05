package Nodes.GENodes;

import Nodes.ExecutableNode;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;


public class GEBuyNode implements ExecutableNode {

    private Script hostScriptRefence;
    private static ExecutableNode singleton;
    private int itemID;
    private String searchTerm;

    private GEBuyNode(Script hostScriptRefence, int itemID, String searchTerm) {
        this.hostScriptRefence = hostScriptRefence;
        this.itemID = itemID;
        this.searchTerm = searchTerm;
    }


    public static ExecutableNode getInstance(Script hostScriptRefence, int itemID, String searchTerm){
        if(hostScriptRefence != null){
            if(singleton == null){
                singleton = new GEBuyNode(hostScriptRefence, itemID, searchTerm);
            }
            return singleton;
        }
        throw new IllegalStateException("script reference is null");
    }


    public static ExecutableNode getInstance(){
        if(singleton != null){
            return singleton;
        }
        throw new IllegalStateException("singleton is null, call the other overloaded getInstance method first");
    }

    @Override
    public int executeNodeAction() throws InterruptedException {

        return 0;
    }

}
