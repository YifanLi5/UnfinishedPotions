package Nodes.BankingNodes.WithdrawNodes;

import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

public class DecideRestockNode implements MarkovNodeExecutor.ExecutableNode {
    private Script script;
    private ComponentsEnum item;
    private boolean goToGE = false;

    public DecideRestockNode(Script script, ComponentsEnum item) {
        this.script = script;
        this.item = item;
    }

    @Override
    public boolean canExecute() {
        return script.getBank().isOpen();
    }

    @Override
    public int executeNode() throws InterruptedException {
        logNode();
        Bank bank = script.getBank();
        int primaryRemaining = (int) bank.getAmount(item.getPrimaryItemName());
        if(primaryRemaining < 14)
            goToGE = true;

        return (int) Statics.randomNormalDist(1000, 200);
    }

    @Override
    public boolean doConditionalTraverse() {
        if(goToGE){
            goToGE = false;
            return true;
        }
        return false;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
