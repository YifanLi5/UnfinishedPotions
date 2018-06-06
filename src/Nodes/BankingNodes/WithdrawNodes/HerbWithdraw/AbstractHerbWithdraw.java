package Nodes.BankingNodes.WithdrawNodes.HerbWithdraw;

import Util.HerbAndPotionsEnum;
import ScriptClasses.MarkovNodeExecutor;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public abstract class AbstractHerbWithdraw implements MarkovNodeExecutor.ExecutableNode{
    Script script;
    HerbAndPotionsEnum targetHerb;

    AbstractHerbWithdraw(Script script, HerbAndPotionsEnum targetHerb){
        this.script = script;
        this.targetHerb = targetHerb;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !script.getInventory().contains(targetHerb.getItemName());
    }

    @Override
    public int executeNode() throws InterruptedException {
        Bank bank = script.getBank();
        if(bank.isOpen())
            if(withdrawHerbs())
                return (int) Statics.randomNormalDist(1000, 500);

        return 0;
    }

    abstract boolean withdrawHerbs();
}
