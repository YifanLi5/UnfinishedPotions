package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import ScriptClasses.MarkovNodeExecutor;
import Util.HerbAndPotionsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

public abstract class AbstractVialWithdraw implements MarkovNodeExecutor.ExecutableNode{
    Script script;

    public AbstractVialWithdraw(Script script) {
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {

        return !script.getInventory().contains(HerbAndPotionsEnum.VIAL_OF_WATER.getItemName());

    }

    @Override
    public int executeNode() throws InterruptedException {
        Bank bank = script.getBank();
        if(bank.isOpen())
            if(withdrawVials())
                return (int) Statics.randomNormalDist(1000, 500);

        return 0;
    }

    abstract boolean withdrawVials();
}
