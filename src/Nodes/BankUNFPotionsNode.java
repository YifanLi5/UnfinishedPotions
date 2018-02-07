package Nodes;

import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_RANARR;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public class BankUNFPotionsNode implements ExecutableNode {

    private Script hostScriptRefence;
    private static ExecutableNode singleton;

    private BankUNFPotionsNode(Script hostScriptRefence){
        this.hostScriptRefence = hostScriptRefence;
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new BankUNFPotionsNode(hostScriptRefence);
        }
        return singleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        Bank bank = hostScriptRefence.getBank();
        Inventory inv = hostScriptRefence.getInventory();

        if(bank.open()){
            if(!inv.isEmpty()){
                bank.depositAll();
            }
            bank.withdraw(VIAL_OF_WATER, 14);
            bank.withdraw(CLEAN_RANARR, 14);
            hostScriptRefence.getWidgets().closeOpenInterface();

        }


        return (int) Statics.randomNormalDist(1000, 300);
    }
}
