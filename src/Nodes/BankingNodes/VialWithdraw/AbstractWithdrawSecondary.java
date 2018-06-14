package Nodes.BankingNodes.VialWithdraw;

import Nodes.BankingNodes.HerbWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.HerbWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.HerbWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.OptionalInvFixNode;
import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractWithdrawSecondary implements ExecutableNode{
    Script script;
    ComponentsEnum components;

    private List<Edge> prePrimaryNodes = Arrays.asList(
            new Edge(Withdraw10Primary.class, 5),
            new Edge(Withdraw14Primary.class, 90),
            new Edge(WithdrawXPrimary.class, 10));

    private List<Edge> postPrimaryNodes = Arrays.asList(
            new Edge(OptionalInvFixNode.class, 70),
            new Edge(AFKCreation.class, 50),
            new Edge(HoverBankerCreation.class, 50),
            new Edge(PrematureStopCreation.class, 30));

    public AbstractWithdrawSecondary(Script script, ComponentsEnum components) {
        this.components = components;
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !script.getInventory().contains(components.getSecondaryItemName());
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        Bank bank = script.getBank();
        if(bank.isOpen()){
            if(bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
                if(withdrawSecondary())
                    return (int) Statics.randomNormalDist(500, 100);
            }
        }


        return 0;
    }

    abstract boolean withdrawSecondary() throws InterruptedException;

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        boolean hasPrimary = script.getInventory().contains(components.getPrimaryItemName());
        return hasPrimary ? postPrimaryNodes : prePrimaryNodes;
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }
}
