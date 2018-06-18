package Nodes.GENodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.GrandExchangeUtil.GrandExchangePolling;
import Util.ItemCombinationRecipes;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class GESpinLockSellNode implements ExecutableNode {
    private Script script;
    private GrandExchangeOperations operations;
    private GrandExchangePolling polling;
    private Margins margins;
    private ItemCombinationRecipes recipe;

    private List<Edge> adjNodes = Collections.singletonList(new Edge(GESpinLockBuyNode.class, 1));

    public GESpinLockSellNode(Script script) {
        this.script = script;
        this.operations = GrandExchangeOperations.getInstance(script.bot);
        this.polling = GrandExchangePolling.getInstance(script);
        margins = Margins.getInstance(script);
        recipe = margins.getCurrentRecipe();
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = script.getNpcs().closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        recipe = margins.getCurrentRecipe();
        if(Statics.logNodes){
            logNode();
        }
        Inventory inv = script.getInventory();
        if(inv.contains(recipe.getFinishedItemName()) || withdrawSellItem(recipe.getFinishedItemID())) {
            if(inv.getAmount(recipe.getFinishedItemName()) >= 50){
                if(isSellItemPending()){
                    script.log("canceling previous sell offer");
                    if(operations.abortOffersWithItem(recipe.getFinishedItemName())){
                        if(collect())
                            script.log("collected aborted offer");
                        else
                            script.warn("failed to collect aborted offer");
                    }
                }
                MethodProvider.sleep(1000);
                if(inv.getAmount(995) < 5000){
                    withdrawCash();
                }
                int[] margin = {0 ,0};
                boolean isConvMargin = false;
                if(inv.getAmount(995) >= 5000){
                    if(margins.getSecondsSinceLastUpdate(recipe) > 900){
                        isConvMargin = true;
                        margin = margins.findSpecificConversionMargin(recipe);
                    } else {
                        isConvMargin = false;
                        margin = margins.findFinishedProductMargin(recipe);
                        margins.updateFinishedProductSellPrice(recipe, margin[0]);
                    }
                }

                spinLockUntilOfferUpdates(margin, isConvMargin);
                if(operations.collect())
                    script.log("collected gp from selling");
                else
                    script.warn("failed to collect gp from offer");

            }
        }
        return 1000;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }

    private void spinLockUntilOfferUpdates(int[] margin, boolean isConvMargin) throws InterruptedException {
        if(selectSellOperation(margin, isConvMargin)) {
            int loops = 0;
            GrandExchange.Box sellingBox = findFinishedProductSellingBox();
            boolean lock = true;
            script.log("Entering spinlocking in GESell");
            while(lock){
                loops++;
                Thread.sleep(1000);
                if(loops > 60){
                    loops = 0;
                    if(decreaseOffer(sellingBox)){
                        script.log("decrease offer successful");
                    } else{
                        script.warn("decrease offer unsuccessful");
                    }
                    lock = doContinueLocking(sellingBox);
                }
            }
            script.log("GESell spinlock released");
            collect();
        }
    }

    private boolean doContinueLocking(GrandExchange.Box sellingBox){
        GrandExchange ge = script.getGrandExchange();
        double percentComplete = ge.getAmountTraded(sellingBox) / ge.getAmountToTransfer(sellingBox);
        return percentComplete < 0.25;
    }

    private GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    private boolean decreaseOffer(GrandExchange.Box abortMe) throws InterruptedException {
        int prevOffer = script.grandExchange.getItemPrice(abortMe);
        script.log("decreasing offer to " + (prevOffer - 25));
        if(operations.abortOffersWithItem(recipe.getFinishedItemName())){
            MethodProvider.sleep(1000);
            if(collect()){
                MethodProvider.sleep(1000);
                return operations.sellAll(recipe.getFinishedNotedItemID(), prevOffer - 25);
            }
        }
        return false;
    }

    private boolean withdrawSellItem(int itemID) throws InterruptedException {
        Bank bank = script.getBank();
        if (bank.open()) {
            boolean success = new ConditionalSleep(1000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if (success) {
                if(bank.getAmount(itemID) >= 50 && bank.enableMode(Bank.BankMode.WITHDRAW_NOTE)){
                    return bank.withdraw(itemID, Bank.WITHDRAW_ALL);
                }
            }
        }
        return false;
    }

    private boolean collect() throws InterruptedException {
        boolean successfulCollect = false;
        int attempts = 0;
        while(!successfulCollect && attempts < 5){
            successfulCollect = operations.collect();
            attempts++;
            MethodProvider.sleep(1000);
        }
        return successfulCollect;
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

    private boolean selectSellOperation(int[] margin, boolean isConvMargin) throws InterruptedException {
        if(margin[0] <= 0 || margin[1] <= 0){
            throw new RuntimeException("price check went wrong: [0,0]");
        }
        if(isConvMargin){
            return operations.sellAll(recipe.getFinishedNotedItemID(), margin[1]);
        } else {
            return operations.sellAll(recipe.getFinishedNotedItemID(), margin[0]);
        }
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

    public void stopThread(){
        if(polling != null)
            polling.stopQueryingOffers();
    }
}
