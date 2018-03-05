package Nodes.BankingNodes;


import Nodes.ExecutableNode;
import ScriptClasses.HerbEnum;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

/*
withdraw 10 vials
then 14 herbs
close and reopen the bank
then 4 vials
used to simulate the player misclicking withdraw 10 herbs instead of 14 and not realizing it until after he has closed the bank.
*/
public class SubOptimalWithdrawVariation3 extends AbstractBankNode {
    public static final int NODE_EXECUTION_WEIGHT = 2;
    private static ExecutableNode singleton;

    private SubOptimalWithdrawVariation3(Script hostScriptReference, HerbEnum herbEnum) {
        super(hostScriptReference, herbEnum);
    }

    public static ExecutableNode getInstance(Script hostScriptReference, HerbEnum herbEnum){
        if(singleton == null) singleton = new SubOptimalWithdrawVariation3(hostScriptReference, herbEnum);
        return singleton;
    }

    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        bank.withdraw(VIAL_OF_WATER, Bank.WITHDRAW_10);
        Statics.shortRandomNormalDelay();
        bank.withdraw(cleanHerb.getItemID(), 14);
        if(bank.close()){
            Statics.longRandomNormalDelay();
            bank.withdrawAll(VIAL_OF_WATER);
        }
    }

    @Override
    public int getDefaultEdgeWeight() {
        return NODE_EXECUTION_WEIGHT;
    }
}
