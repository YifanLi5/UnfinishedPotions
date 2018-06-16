package Nodes;

import Nodes.GENodes.GESpinLockBuyNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ConversionMargins;
import Util.GrandExchangeUtil.GrandExchangeObserver;
import Util.UnfPotionRecipes;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

import java.util.List;


public class DebuggingNode implements GrandExchangeObserver, ExecutableNode {

    Script script;
    ConversionMargins margins;
    GESpinLockBuyNode buy;

    public DebuggingNode(Script script) {
        this.script = script;
        margins = ConversionMargins.getInstance(script);
        buy = new GESpinLockBuyNode(script);
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
        margins.priceCheckAll();
        script.log("time since lastUpdate: " + margins.getSecondsSinceLastUpdate(UnfPotionRecipes.TOADFLAX));
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

    public void stop(){
        buy.stopThread();
    }
}
