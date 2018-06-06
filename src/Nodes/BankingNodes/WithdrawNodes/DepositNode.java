package Nodes.BankingNodes.WithdrawNodes;

import ScriptClasses.MarkovNodeExecutor;
import Util.HerbAndPotionsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;


public class DepositNode implements MarkovNodeExecutor.ExecutableNode {
    private Script script;
    public DepositNode(Script script){
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        Inventory inv = script.getInventory();
        return inv.contains(995)
                || inv.contains(HerbAndPotionsEnum.AVANTOE.getUnfPotionName())
                || inv.contains(HerbAndPotionsEnum.TOADFLAX.getUnfPotionName())
                || inv.contains(HerbAndPotionsEnum.RANARR.getUnfPotionName());
    }

    @Override
    public int executeNode() throws InterruptedException {
        Bank bank = script.getBank();
        if(bank.open()){
            new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if(bank.isOpen()){
                if(bank.depositAll()){
                    return (int) Statics.randomNormalDist(1000, 500);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
