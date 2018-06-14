package Nodes.BankingNodes.HerbWithdraw;

import Nodes.BankingNodes.OptionalInvFixNode;
import Nodes.BankingNodes.VialWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.VialWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.VialWithdraw.WithdrawXSecondary;
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

public abstract class AbstractWithdrawPrimary implements ExecutableNode{
    Script script;
    ComponentsEnum components;

    private boolean alreadyWithdrawnSecondary;
    private List<Edge> preSecondaryEdges = Arrays.asList(
            new Edge(Withdraw10Secondary.class, 5),
            new Edge(Withdraw14Secondary.class, 90),
            new Edge(WithdrawXSecondary.class, 10));

    private List<Edge> postSecondaryEdges = Arrays.asList(
            new Edge(OptionalInvFixNode.class, 70),
            new Edge(AFKCreation.class, 50),
            new Edge(HoverBankerCreation.class, 50),
            new Edge(PrematureStopCreation.class, 30));

    AbstractWithdrawPrimary(Script script, ComponentsEnum component){
        this.script = script;
        this.components = component;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !script.getInventory().contains(components.getPrimaryItemName())
                || script.getInventory().isEmpty();
    }

    @Override
    public int executeNode() throws InterruptedException {
        //logNode();
        Bank bank = script.getBank();
        if(bank.isOpen()){
            alreadyWithdrawnSecondary = script.getInventory().contains(components.getSecondaryItemName());
            if(bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
                if(withdrawPrimary())
                    return (int) Statics.randomNormalDist(500, 100);
            }
        }

        return 0;
    }

    abstract boolean withdrawPrimary() throws InterruptedException;

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return alreadyWithdrawnSecondary ? postSecondaryEdges : preSecondaryEdges;
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
