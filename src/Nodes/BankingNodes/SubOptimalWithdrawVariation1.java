package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

/*
withdraw 10 vials, then 14 herbs, then 4vials
used to simulate the player misclicking withdraw 10 on vials
*/
public class SubOptimalWithdrawVariation1 extends AbstractBankNode {

    private static ExecutableNode singleton;
    private SubOptimalWithdrawVariation1(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new SubOptimalWithdrawVariation1(hostScriptRefence);
        }
        return singleton;
    }


    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        bank.withdraw(VIAL_OF_WATER, Bank.WITHDRAW_10);
        Statics.shortRandomNormalDelay();
        bank.withdraw(CLEAN_HERB, 14);
        Statics.longRandomNormalDelay();
        bank.withdrawAll(VIAL_OF_WATER);
    }
}
