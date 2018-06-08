package Nodes.BankingNodes.WithdrawNodes.HerbWithdraw;

import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

public abstract class AbstractPrimaryComponentWithdraw implements MarkovNodeExecutor.ExecutableNode{
    Script script;
    ComponentsEnum components;

    AbstractPrimaryComponentWithdraw(Script script, ComponentsEnum primary){
        this.script = script;
        this.components = primary;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !script.getInventory().contains(components.getPrimaryItemName())
                || script.getInventory().isEmpty();
    }

    @Override
    public int executeNode() throws InterruptedException {
        Bank bank = script.getBank();
        if(bank.isOpen())
            if(withdrawHerbs())
                return (int) Statics.randomNormalDist(500, 100);

        return 0;
    }

    abstract boolean withdrawHerbs();
}
