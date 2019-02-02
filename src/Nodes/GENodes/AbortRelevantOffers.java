package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.Margins;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class AbortRelevantOffers extends MethodProvider implements ExecutableNode {
    private boolean isJumping = false;
    private CombinationRecipes recipe;
    private GrandExchangeOperations operations;

    public AbortRelevantOffers(Bot bot){
        exchangeContext(bot);
        operations = GrandExchangeOperations.getInstance(bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        this.recipe = Margins.getInstance(bot).getCurrentRecipe();
        NPC clerk = npcs.closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        logNode();
        if(isBuyItemPending()){
            log("buy offer is pending, preparing to abort or collect");
            boolean abortSuccessful = new ConditionalSleep(5000, 1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return operations.abortOffersWithItem(recipe.getPrimary());
                }
            }.sleep();
            if(abortSuccessful){
                boolean collected = new ConditionalSleep(5000, 1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return operations.collect(true);
                    }
                }.sleep();
                if(collected) {
                    log("aborted and/or collected offers for: " + recipe.getPrimary());
                    int remainingPrimary = (int) inventory.getAmount(recipe.getPrimary());
                    if(remainingPrimary > 0) {
                        log("finishing up remaining " + remainingPrimary + " " + recipe.getPrimary().getName());
                        isJumping = true;
                    }
                }
            }
        }

        if(isSellItemPending()){
            log("sell offer is pending, preparing to abort or collect");
            boolean abortSuccessful = new ConditionalSleep(5000, 1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return operations.abortOffersWithItem(recipe.getProduct());
                }
            }.sleep();
            if(abortSuccessful){
                boolean collected = new ConditionalSleep(5000, 1000){
                    @Override
                    public boolean condition() {
                        return operations.collect(true);
                    }
                }.sleep();
                if(collected)
                    log("aborted and/or collected offers for " + recipe.getProduct());

            }
        }

        return randomNormalDist(500, 200);
    }

    private boolean isBuyItemPending(){
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(grandExchange.getItemId(box) == recipe.getPrimary().getId()){
                log(grandExchange.getStatus(box));
                if(grandExchange.getStatus(box) == GrandExchange.Status.PENDING_BUY ||
                        grandExchange.getStatus(box) == GrandExchange.Status.COMPLETING_BUY ||
                        grandExchange.getStatus(box) == GrandExchange.Status.FINISHED_BUY){
                    return true;
                }
            }
        return false;
    }

    private boolean isSellItemPending(){
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(grandExchange.getItemId(box) == recipe.getProduct().getId()){
                if(grandExchange.getStatus(box) == GrandExchange.Status.COMPLETING_SALE ||
                        grandExchange.getStatus(box) == GrandExchange.Status.PENDING_SALE ||
                        grandExchange.getStatus(box) == GrandExchange.Status.FINISHED_SALE){
                    log("sell is pending");
                    return true;
                }

            }
        return false;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(Sell.class, 1));
    }

    @Override
    public boolean isJumping() {
        if(isJumping){
            isJumping = false;
            return true;
        }
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return DepositNode.class;
    }

    @Override
    public void logNode() {
        log(this.getClass().getSimpleName());
    }
}
