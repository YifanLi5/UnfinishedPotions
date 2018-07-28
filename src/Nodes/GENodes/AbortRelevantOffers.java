package Nodes.GENodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class AbortRelevantOffers implements ExecutableNode {
    private final Script script;
    private CombinationRecipes recipe;
    private GrandExchangeOperations operations;

    public AbortRelevantOffers(Script script){
        this.script = script;
        operations = GrandExchangeOperations.getInstance(script.bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = script.getNpcs().closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        recipe = Margins.getInstance(script).getCurrentRecipe();
        logNode();
        if(isBuyItemPending()){
            script.log("buy offer is pending, preparing to abort or collect");
            boolean abortSuccessful = new ConditionalSleep(5000, 1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return operations.abortOffersWithItem(recipe.getPrimaryItemName());
                }
            }.sleep();
            if(abortSuccessful){
                boolean collected = new ConditionalSleep(5000, 1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return operations.collect(true);
                    }
                }.sleep();
                if(collected)
                    script.log("aborted and/or collected offers for: " + recipe.getPrimaryItemName());
            }
        }

        if(isSellItemPending()){
            script.log("sell offer is pending, preparing to abort or collect");
            boolean abortSuccessful = new ConditionalSleep(5000, 1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return operations.abortOffersWithItem(recipe.getFinishedItemName());
                }
            }.sleep();
            if(abortSuccessful){
                boolean collected = new ConditionalSleep(5000, 1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return operations.collect(true);
                    }
                }.sleep();
                if(collected)
                    script.log("aborted and/or collected offers for " + recipe.getFinishedItemName());

            }
        }

        return (int) Statics.randomNormalDist(500, 200);
    }

    private boolean isBuyItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(ge.getItemId(box) == recipe.getPrimaryItemID()){
                script.log(ge.getStatus(box));
                if(ge.getStatus(box) == GrandExchange.Status.PENDING_BUY ||
                        ge.getStatus(box) == GrandExchange.Status.COMPLETING_BUY ||
                        ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY){
                    return true;
                }
            }
        return false;
    }

    private boolean isSellItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(ge.getItemId(box) == recipe.getFinishedItemID()){
                if(ge.getStatus(box) == GrandExchange.Status.COMPLETING_SALE ||
                        ge.getStatus(box) == GrandExchange.Status.PENDING_SALE ||
                        ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE){
                    script.log("sell is pending");
                    return true;
                }

            }
        return false;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(GESpinLockSellNode.class, 1));
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
