package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.ItemCombinationRecipes;
import Util.Margins;
import Util.Statics;
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
    private ItemCombinationRecipes recipe;
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
        if(cachedMargin == null || cachedMargin[0] >= 100000 || cachedMargin[1] <= 0)
            cachedMargin = margins.findPrimaryIngredientMargin(recipe);
        script.log("withdraw cash");
        if(withdrawCash()){
            if(openGE()){

            }

            GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
            script.log("buying box: " + buyingBox);
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
                script.log("selling box: " + buyingBox);
                if(sellingBox != null){
                    int soldFinishedProducts = operations.getAmountTraded(sellingBox);
                    if(soldFinishedProducts >= 0)
                        operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), (cachedMargin[0] + cachedMargin[1]) / 2, soldFinishedProducts);
                } else operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), (cachedMargin[0] + cachedMargin[1]) / 2, 500);
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
                int bankedCoins = (int) bank.getAmount(995);
                if(bankedCoins > 10000){
                    int withdrawAmt = (bankedCoins - 10000)/ 1000 * 1000;
                    if(withdrawAmt == 0){
                      return true;
                    } else return bank.withdraw(995, withdrawAmt) && bank.close();
                } else return bankedCoins < 10000 && script.getInventory().contains(995);
            }
        }
        return false;
    }

    private boolean openGE() {
        GrandExchange ge = script.getGrandExchange();
        if(!ge.isOpen()){
            NPC grandExchangeClerk = script.getNpcs().closest("Grand Exchange Clerk");
            if(grandExchangeClerk != null){
                boolean didInteraction = grandExchangeClerk.interact("Exchange");
                return new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return didInteraction && ge.isOpen();
                    }
                }.sleep();
            }
        }
        return true;
    }

    private GrandExchange.Box findPrimaryIngredientBuyingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getPrimaryItemID()){
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
