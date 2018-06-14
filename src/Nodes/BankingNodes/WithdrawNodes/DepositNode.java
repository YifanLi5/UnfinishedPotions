package Nodes.BankingNodes.WithdrawNodes;

import ScriptClasses.MarkovNodeExecutor;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;


public class DepositNode implements MarkovNodeExecutor.ExecutableNode {
    private Script script;
    public DepositNode(Script script){
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return script.getNpcs().closestThatContains("Banker").exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        //logNode();
        Bank bank = script.getBank();
        if(bank.open()){
            boolean success = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if(success){
                if(!script.getInventory().isEmpty()){
                    if(bank.depositAll()){
                        return (int) Statics.randomNormalDist(500, 250);
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
