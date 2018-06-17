package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.Margins;
import Util.Statics;
import Util.UnfPotionRecipes;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class IntermittentBuy implements ExecutableNode{

    private Script script;
    private Margins margins;
    private UnfPotionRecipes recipe;
    private GrandExchangeOperations operations;

    public IntermittentBuy(Script script) {
        this.script = script;
        margins = Margins.getInstance(script);
        recipe = margins.getCurrentRecipe();
        operations = GrandExchangeOperations.getInstance(script.bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = script.getNpcs().closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        script.log("executing IntermittentBuy");
        recipe = margins.getCurrentRecipe();
        int[] cachedMargin = margins.getCachedPrimaryIngredientMargin(recipe);
        if(cachedMargin == null || cachedMargin[0] <= 0 || cachedMargin[1] <= 0)
            cachedMargin = margins.findPrimaryIngredientMargin(recipe);
        if(withdrawCash()){
            GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
            if(buyingBox != null){
                if(operations.abortOffersWithItem(recipe.getPrimaryItemName())){
                    Statics.longRandomNormalDelay();
                    if(operations.collect()){
                        Statics.shortRandomNormalDelay();
                        increaseOffer(cachedMargin, buyingBox);
                    }
                }
            }
            else{
                GrandExchange.Box sellingBox = findFinishedProductSellingBox();
                if(sellingBox != null){
                    int soldFinishedProducts = operations.getAmountTraded(sellingBox);
                    operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), (cachedMargin[0] + cachedMargin[1]) / 2, soldFinishedProducts);
                }
                operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), (cachedMargin[0] + cachedMargin[1]) / 2, 500);
            }
        }


        return (int) Statics.randomNormalDist(1500, 500);
    }

    private boolean increaseOffer(int[] margin, GrandExchange.Box buyingBox) throws InterruptedException {
        if(buyingBox != null){
            int prevOffer = operations.getItemPrice(buyingBox);
            int incrementFactor = (margin[1] - margin[0]) / 4;
            int newBuyPrice = prevOffer + incrementFactor;
            script.log("increasing buy offer to " + newBuyPrice);
            if(operations.abortOffersWithItem(recipe.getFinishedItemName())){
                MethodProvider.sleep(1000);
                if(operations.collect()){
                    MethodProvider.sleep(1000);
                    GrandExchange.Box sellingBox = findFinishedProductSellingBox();
                    if(sellingBox != null){
                        int soldFinishedProducts = operations.getAmountTraded(sellingBox);
                        return operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), newBuyPrice, soldFinishedProducts);
                    }
                    return operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), newBuyPrice, 500);
                }
            }
        }
        return false;
    }

    private boolean withdrawCash() throws InterruptedException {
        Bank bank = script.getBank();
        if(bank.open()){
            boolean success = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if(success){
                if(bank.getAmount(995) >= 0){
                    return bank.withdraw(995, Bank.WITHDRAW_ALL) && bank.close();
                }
            }
        }
        return false;
    }

    private GrandExchange.Box findPrimaryIngredientBuyingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    private GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(DepositNode.class, 1));
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
}
