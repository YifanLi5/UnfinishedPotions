package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

abstract class AbstractBankNode implements ExecutableNode {

    Script hostScriptRefence;

    AbstractBankNode(Script hostScriptRefence){
        this.hostScriptRefence = hostScriptRefence;
    }


    @Override
    public int executeNodeAction() throws InterruptedException {
        logNode();
        if(cycleInventory()){
            hostScriptRefence.getWidgets().closeOpenInterface();
            return 0;
        }
        return 1500;
    }

    private boolean cycleInventory() throws InterruptedException {
        Bank bank = hostScriptRefence.getBank();
        Inventory inv = hostScriptRefence.getInventory();
        if(bank.open()) {
            MethodProvider.sleep(500);
            bank.depositAll();
            if (bank.contains(CLEAN_HERB) && bank.contains(VIAL_OF_WATER)) {
                withdrawOrder();
                return true;
            }
            else{
                hostScriptRefence.log("ran out of supplies stopping script, TODO: rebuy");
                hostScriptRefence.stop(false);
            }
        }
        return false;
    }



    void logNode(){
        hostScriptRefence.log(this.getClass().getSimpleName());
    }

    //defined in subclasses, used to determine in what order to withdraw vials and herbs
    abstract void withdrawOrder() throws InterruptedException;
}
