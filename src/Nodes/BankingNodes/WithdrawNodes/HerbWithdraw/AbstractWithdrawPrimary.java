package Nodes.BankingNodes.WithdrawNodes.HerbWithdraw;

import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.script.Script;

public abstract class AbstractWithdrawPrimary implements MarkovNodeExecutor.ExecutableNode{
    Script script;
    ComponentsEnum components;

    AbstractWithdrawPrimary(Script script, ComponentsEnum component){
        this.script = script;
        this.components = component;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !script.getInventory().contains(components.getPrimaryItemName())
                || script.getInventory().isEmpty();
    }

    @Override
    public int executeNode() throws InterruptedException {
        logNode();
        if(script.getBank().isOpen()){
            if(withdrawPrimary())
                return (int) Statics.randomNormalDist(500, 100);
        }

        return 0;
    }

    abstract boolean withdrawPrimary() throws InterruptedException;

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
