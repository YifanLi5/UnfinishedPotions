package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import ScriptClasses.HerbEnum;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public class OptimalWithdrawVariation1 extends AbstractBankNode{
    public static final int NODE_EXECUTION_WEIGHT = 46;
    private static ExecutableNode singleton;

    private OptimalWithdrawVariation1(Script hostScriptReference, HerbEnum herbEnum) {
        super(hostScriptReference, herbEnum);
    }

    public static ExecutableNode getInstance(Script hostScriptReference, HerbEnum herbEnum){
        if(singleton == null) singleton = new OptimalWithdrawVariation1(hostScriptReference, herbEnum);
        return singleton;
    }

    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        bank.withdraw(VIAL_OF_WATER, 14);
        Statics.shortRandomNormalDelay();
        bank.withdraw(cleanHerb.getItemID(), 14);
    }

    @Override
    public int getDefaultEdgeWeight() {
        return NODE_EXECUTION_WEIGHT;
    }
}
