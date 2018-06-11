package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.script.Script;

public abstract class AbstractSecondaryWithdraw implements MarkovNodeExecutor.ExecutableNode{
    Script script;
    ComponentsEnum components;

    public AbstractSecondaryWithdraw(Script script, ComponentsEnum components) {
        this.components = components;
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !script.getInventory().contains(components.getSecondaryItemName());
    }

    @Override
    public int executeNode() throws InterruptedException {
        logNode();
        if(script.getBank().isOpen()){
            if(!script.getInventory().isEmptyExcept(components.getPrimaryItemName()))
                script.getBank().depositAll();
            if(withdrawSecondary())
                return (int) Statics.randomNormalDist(500, 100);
        }


        return 0;
    }

    abstract boolean withdrawSecondary();

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
