package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;
import static ScriptClasses.Statics.randomNormalDist;

public class HerbFirstWithdraw extends AbstractBankNode {
    private static ExecutableNode singleton;

    private HerbFirstWithdraw(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new HerbFirstWithdraw(hostScriptRefence);
        }
        return singleton;
    }

    @Override
    void withdrawOrder() throws InterruptedException {
        Bank bank = hostScriptRefence.getBank();
        bank.withdraw(CLEAN_HERB, 14);
        MethodProvider.sleep(randomNormalDist(300,100));
        bank.withdraw(VIAL_OF_WATER, 14);
    }

}
