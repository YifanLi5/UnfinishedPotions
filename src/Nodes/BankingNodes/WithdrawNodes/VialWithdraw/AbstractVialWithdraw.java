package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.HerbAndPotionsEnum;
import ScriptClasses.MarkovNodeExecutor;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public abstract class AbstractVialWithdraw implements MarkovNodeExecutor.ExecutableNode{
    Script script;

    public AbstractVialWithdraw(Script script) {
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        Bank bank = script.getBank();
        if(script.getInventory().contains(HerbAndPotionsEnum.VIAL_OF_WATER.getItemName())){
            return false;
        }
        if(bank.open()){
            new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if(bank.isOpen())
                return bank.contains(HerbAndPotionsEnum.VIAL_OF_WATER.getItemName());

        }
        return false;
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
