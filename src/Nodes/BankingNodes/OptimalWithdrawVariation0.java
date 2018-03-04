package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

/*
Herb first, then potions
*/
public class OptimalWithdrawVariation0 extends AbstractBankNode {
    private static ExecutableNode singleton;

    private OptimalWithdrawVariation0(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new OptimalWithdrawVariation0(hostScriptRefence);
        }
        return singleton;
    }

    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptReference.getBank();
        Statics.shortRandomNormalDelay();
        bank.withdraw(CLEAN_HERB, 14);
        Statics.shortRandomNormalDelay();
        bank.withdraw(VIAL_OF_WATER, 14);
    }

}
