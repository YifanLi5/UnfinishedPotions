package Nodes.BankingNodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.List;


public class DepositNode implements ExecutableNode {
    private Script script;
    private List<Edge> adjNodes = Arrays.asList(new Edge(DecideRestockNode.class, 1));

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
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
