package Nodes.BankingNodes;

import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;

import java.util.Arrays;
import java.util.List;

public class OptionalInvFixNode implements ExecutableNode{
    private ComponentsEnum components;
    private Script script;

    private List<Edge> adjNodes = Arrays.asList(
            new Edge(AFKCreation.class, 50),
            new Edge(HoverBankerCreation.class, 50),
            new Edge(PrematureStopCreation.class, 10));

    public OptionalInvFixNode(Script script, ComponentsEnum components) {
        this.components = components;
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        Inventory inv = script.getInventory();
        return inv.getAmount(components.getPrimaryItemName()) != 14
                || inv.getAmount(components.getSecondaryItemName()) != 14;
    }

    @Override
    public int executeNode() throws InterruptedException {
        //logNode();
        Inventory inv = script.getInventory();
        Bank bank = script.getBank();
        if(bank.isOpen()){
            if(inv.getAmount(components.getPrimaryItemName()) != 14
                    && inv.getAmount(components.getSecondaryItemName()) != 14){
                if(bank.depositAll()){
                    if(script.getBank().withdraw(components.getPrimaryItemName(), 14)){
                        Statics.shortRandomNormalDelay();
                        script.getBank().withdraw(components.getSecondaryItemName(), 14);
                    }
                }
            } else if(inv.getAmount(components.getPrimaryItemName()) != 14){
                if(script.getBank().withdraw(components.getPrimaryItemName(), 14)){

                }
            } else {
                script.getBank().withdraw(components.getSecondaryItemName(), 14);
            }
        }
        return (int) Statics.randomNormalDist(600, 200);
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
