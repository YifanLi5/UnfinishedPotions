package Nodes.BankingNodes;

import Nodes.BankingNodes.HerbWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.HerbWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.HerbWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.VialWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.VialWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.VialWithdraw.WithdrawXSecondary;
import Nodes.GENodes.GESellNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import java.util.Arrays;
import java.util.List;

public class DecideRestockNode implements ExecutableNode {
    private Script script;
    private ComponentsEnum item;
    private boolean goToGE = false;

    private List<Edge> adjNodes;

    public DecideRestockNode(Script script, ComponentsEnum item) {
        this.script = script;
        this.item = item;

        adjNodes = Arrays.asList(
                new Edge(Withdraw10Primary.class, 5),
                new Edge(Withdraw14Primary.class, 90),
                new Edge(WithdrawXPrimary.class, 10),
                new Edge(Withdraw10Secondary.class, 5),
                new Edge(Withdraw14Secondary.class, 90),
                new Edge(WithdrawXSecondary.class, 10
                ));
    }

    @Override
    public boolean canExecute() {
        return script.getBank().isOpen();
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        Bank bank = script.getBank();
        int primaryRemaining = (int) bank.getAmount(item.getPrimaryItemName());
        if(primaryRemaining < 14)
            goToGE = true;

        return 0;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }

    @Override
    public boolean isJumping() {
        if(goToGE){
            goToGE = false;
            return true;
        }
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return GESellNode.class;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
