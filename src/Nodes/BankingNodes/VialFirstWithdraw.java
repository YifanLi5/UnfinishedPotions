package Nodes.BankingNodes;

import Nodes.ExecutableNode;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public class VialFirstWithdraw extends AbstractBankNode{

    private static ExecutableNode singleton;
    private VialFirstWithdraw(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new VialFirstWithdraw(hostScriptRefence);
        }
        return singleton;
    }

    void withdrawOrder(){
        Bank bank = hostScriptRefence.getBank();
        bank.withdraw(VIAL_OF_WATER, 14);
        bank.withdraw(CLEAN_HERB, 14);
    }
}
