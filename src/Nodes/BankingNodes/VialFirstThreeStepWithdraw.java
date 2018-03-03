package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;
import static ScriptClasses.Statics.randomNormalDist;

/*
VialFirstThreeStepWithdraw means to withdraw 10 vials, then 14 herbs, then 4vials
used to simulate the player misclicking withdraw 14
*/
public class VialFirstThreeStepWithdraw extends AbstractBankNode {

    private static ExecutableNode singleton;
    private VialFirstThreeStepWithdraw(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new VialFirstThreeStepWithdraw(hostScriptRefence);
        }
        return singleton;
    }


    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptRefence.getBank();
        bank.withdraw(VIAL_OF_WATER, Bank.WITHDRAW_10);
        MethodProvider.sleep(randomNormalDist(300,100));
        bank.withdraw(CLEAN_HERB, 14);
        MethodProvider.sleep(randomNormalDist(1500,250));
        bank.withdrawAll(VIAL_OF_WATER);
    }
}
