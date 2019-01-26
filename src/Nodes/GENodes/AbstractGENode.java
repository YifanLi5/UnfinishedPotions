package Nodes.GENodes;

import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.ItemData;
import Util.Margins;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

abstract class AbstractGENode extends MethodProvider implements ExecutableNode {
    GrandExchangeOperations operations;
    Margins margins;
    CombinationRecipes recipe;

    AbstractGENode(Bot bot) {
        operations = GrandExchangeOperations.getInstance(bot);
        margins = Margins.getInstance(bot);
        exchangeContext(bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        recipe = margins.getCurrentRecipe();
        NPC clerk = npcs.closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    GrandExchange.Box findPrimaryIngredientBuyingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == margins.getCurrentRecipe().getPrimary().getId()){
                return box;
            }
        }
        warn("Expected to find the primary ingredient " + margins.getCurrentRecipe() + " but it didn't exist!");
        return null;
    }

    GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getProduct().getId()){
                return box;
            }
        }
        return null;
    }

    boolean isBuyItemPending(){
        if(margins.getCurrentRecipe() != null) {
            for (GrandExchange.Box box : GrandExchange.Box.values()){
                if (grandExchange.getItemId(box) == margins.getCurrentRecipe().getPrimary().getId()) {
                    log(grandExchange.getStatus(box));
                    if (grandExchange.getStatus(box) == GrandExchange.Status.PENDING_BUY ||
                            grandExchange.getStatus(box) == GrandExchange.Status.COMPLETING_BUY ||
                            grandExchange.getStatus(box) == GrandExchange.Status.FINISHED_BUY) {
                        log("buy is pending");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean isSellItemPending(){
        if(margins.getCurrentRecipe() != null){
            for (GrandExchange.Box box : GrandExchange.Box.values()){
                if (grandExchange.getItemId(box) == margins.getCurrentRecipe().getProduct().getId()) {
                    if (grandExchange.getStatus(box) == GrandExchange.Status.COMPLETING_SALE ||
                            grandExchange.getStatus(box) == GrandExchange.Status.PENDING_SALE ||
                            grandExchange.getStatus(box) == GrandExchange.Status.FINISHED_SALE) {
                        log("sell is pending");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean withdrawSellItem(ItemData item) throws InterruptedException {
        if (bank.open()) {
            boolean success = new ConditionalSleep(1000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if (success) {
                if(bank.getAmount(item) >= 50 && bank.enableMode(Bank.BankMode.WITHDRAW_NOTE)){
                    return bank.withdraw(item, Bank.WITHDRAW_ALL);
                } else return inventory.contains(item);
            }
        }
        return false;
    }

    @Override
    public void logNode() {
        log("Executing: " + this.getClass().getSimpleName());
    }


}
