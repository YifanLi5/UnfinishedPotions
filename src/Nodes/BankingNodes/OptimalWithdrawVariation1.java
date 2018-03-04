package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public class OptimalWithdrawVariation1 extends AbstractBankNode{

    private static ExecutableNode singleton;
    private OptimalWithdrawVariation1(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new OptimalWithdrawVariation1(hostScriptRefence);
        }
        return singleton;
    }

    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        bank.withdraw(VIAL_OF_WATER, 14);
        Statics.shortRandomNormalDelay();
        bank.withdraw(CLEAN_HERB, 14);
    }
}
