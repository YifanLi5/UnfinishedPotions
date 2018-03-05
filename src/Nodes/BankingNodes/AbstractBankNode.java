package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import ScriptClasses.HerbEnum;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.ArrayList;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public abstract class AbstractBankNode implements ExecutableNode {

    Script hostScriptReference;
    HerbEnum cleanHerb;

    AbstractBankNode(Script hostScriptReference, HerbEnum cleanHerb){
        this.hostScriptReference = hostScriptReference;
        this.cleanHerb =  cleanHerb;
    }

    public static ArrayList<ExecutableNode> getInheritingNodes(Script hostScriptReference, HerbEnum cleanHerb){
        ArrayList<ExecutableNode> bankNodes = new ArrayList<>();
        bankNodes.add(OptimalWithdrawVariation0.getInstance(hostScriptReference, cleanHerb));
        bankNodes.add(OptimalWithdrawVariation1.getInstance(hostScriptReference, cleanHerb));
        bankNodes.add(SubOptimalWithdrawVariation0.getInstance(hostScriptReference, cleanHerb));
        bankNodes.add(SubOptimalWithdrawVariation1.getInstance(hostScriptReference, cleanHerb));
        bankNodes.add(SubOptimalWithdrawVariation2.getInstance(hostScriptReference, cleanHerb));
        bankNodes.add(SubOptimalWithdrawVariation3.getInstance(hostScriptReference, cleanHerb));
        return bankNodes;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        logNode();
        if(cycleInventory()){
            MethodProvider.sleep(Statics.randomNormalDist(600, 100));
            hostScriptReference.getWidgets().closeOpenInterface();
            return 0;
        }
        return 1500;
    }

    private boolean cycleInventory() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        Inventory inv = hostScriptReference.getInventory();
        if(bank.open()) {
            MethodProvider.sleep(Statics.randomNormalDist(500,100));
            bank.depositAll();
            boolean debug1 = bank.contains(CLEAN_HERB);
            boolean debug2 = bank.contains(VIAL_OF_WATER);
            if (debug1 && debug2) {
                withdrawOrder();
                return true;
            }
            else{
                hostScriptReference.log("herb: " + debug1 + " vial: " + debug2);
                hostScriptReference.log("ran out of supplies stopping script, TODO: rebuy");
                hostScriptReference.stop(false);
            }
        }
        return false;
    }



    void logNode(){
        hostScriptReference.log(this.getClass().getSimpleName());
    }

    //defined in subclasses, used to determine in what order to withdraw vials and herbs
    abstract void withdrawOrder() throws InterruptedException;
}
