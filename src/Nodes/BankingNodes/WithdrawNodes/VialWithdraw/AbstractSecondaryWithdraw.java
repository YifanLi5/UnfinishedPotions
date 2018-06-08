package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
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
        Bank bank = script.getBank();
        if(bank.isOpen())
            if(withdrawVials())
                return (int) Statics.randomNormalDist(500, 100);

        return 0;
    }

    abstract boolean withdrawVials();
}
