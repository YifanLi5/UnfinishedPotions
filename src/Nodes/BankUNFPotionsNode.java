package Nodes;

import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;

import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.Statics.CLEAN_HERB;
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
            if(!(inv.contains(CLEAN_HERB) && inv.contains(VIAL_OF_WATER))){
                bank.depositAll();
            }
            if(bank.contains(CLEAN_HERB) && bank.contains(VIAL_OF_WATER)){
                boolean herbFirst = ThreadLocalRandom.current().nextBoolean();
                if(herbFirst){
                    bank.withdraw(CLEAN_HERB, 14);
                    bank.withdraw(VIAL_OF_WATER, 14);
                }
                else{
                    bank.withdraw(VIAL_OF_WATER, 14);
                    bank.withdraw(CLEAN_HERB, 14);
                }
                hostScriptRefence.getWidgets().closeOpenInterface();
            }
            else{
                hostScriptRefence.log("ran out of supplies stopping script, TODO: rebuy");
                hostScriptRefence.stop(false);
            }

        }


        return 0;
    }
}
