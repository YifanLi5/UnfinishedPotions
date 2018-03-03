package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;
import static ScriptClasses.Statics.randomNormalDist;

public class HerbFirstThreeStepWithdraw extends AbstractBankNode{

    private static ExecutableNode singleton;

    private HerbFirstThreeStepWithdraw(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new HerbFirstThreeStepWithdraw(hostScriptRefence);
        }
        return singleton;
    }

    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptRefence.getBank();
        bank.withdraw(CLEAN_HERB, Bank.WITHDRAW_10);
        MethodProvider.sleep(randomNormalDist(300,100));
        bank.withdraw(VIAL_OF_WATER, 14);
        MethodProvider.sleep(randomNormalDist(1500,250));
        bank.withdrawAll(CLEAN_HERB);
    }
}
