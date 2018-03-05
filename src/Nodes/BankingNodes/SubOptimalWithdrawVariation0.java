package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import ScriptClasses.HerbEnum;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;
/*
withdraw 10 herbs, then 14 vials, then 4 herbs
used to simulate the player misclicking withdraw 10 herbs instead of 14.
*/
public class SubOptimalWithdrawVariation0 extends AbstractBankNode{
    public static final int NODE_EXECUTION_WEIGHT = 2;
    private static ExecutableNode singleton;

    private SubOptimalWithdrawVariation0(Script hostScriptReference, HerbEnum herbEnum) {
        super(hostScriptReference, herbEnum);
    }

    public static ExecutableNode getInstance(Script hostScriptReference, HerbEnum herbEnum){
        if(singleton == null) singleton = new SubOptimalWithdrawVariation0(hostScriptReference, herbEnum);
        return singleton;
    }

    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        bank.withdraw(cleanHerb.getItemID(), Bank.WITHDRAW_10);
        Statics.shortRandomNormalDelay();
        bank.withdraw(VIAL_OF_WATER, 14);
        Statics.longRandomNormalDelay();
        bank.withdrawAll(cleanHerb.getItemID());
    }

    @Override
    public int getDefaultEdgeWeight() {
        return NODE_EXECUTION_WEIGHT;
    }
}
