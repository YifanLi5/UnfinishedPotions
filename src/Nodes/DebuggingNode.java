package Nodes;

import Nodes.GENodes.IntermittentBuy;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.GrandExchangeUtil.GrandExchangeObserver;
import Util.ItemCombinationRecipes;
import Util.Margins;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

import java.util.List;


public class DebuggingNode implements GrandExchangeObserver, ExecutableNode {

    Script script;
    IntermittentBuy buy;

    public DebuggingNode(Script script) {
        this.script = script;
        buy = new IntermittentBuy(script);
        Margins.getInstance(script).setCurrentRecipe(ItemCombinationRecipes.IRIT);
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        script.log("box: " + box.toString() + "status: " + ge.getStatus(box) +ge.getAmountTraded(box) + "/" + ge.getAmountToTransfer(box) + " complete");

    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return true;
    }

    @Override
    public int executeNode() throws InterruptedException {
        buy.executeNode();
        return 5000;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return null;
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

    }

    public void stop(){}

}
