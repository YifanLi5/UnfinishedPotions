package Nodes.BankingNodes;


import Nodes.ExecutableNode;
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
    private static ExecutableNode singleton;
    private SubOptimalWithdrawVariation3(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new SubOptimalWithdrawVariation3(hostScriptRefence);
        }
        return singleton;
    }

    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        bank.withdraw(VIAL_OF_WATER, Bank.WITHDRAW_10);
        Statics.shortRandomNormalDelay();
        bank.withdraw(CLEAN_HERB, 14);
        if(bank.close()){
            Statics.longRandomNormalDelay();
            bank.withdrawAll(VIAL_OF_WATER);
        }
    }
}
