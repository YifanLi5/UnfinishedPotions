package Nodes.BankingNodes;

import Nodes.BankingNodes.PrimaryWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.PrimaryWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.PrimaryWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.WithdrawXSecondary;
import Nodes.GENodes.AbortRelevantOffers;
import Nodes.GENodes.InitialBuy;
import Nodes.GENodes.IntermittentBuy;
import Nodes.GENodes.IntermittentSell;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.event.RandomExecutor;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.ScriptPaint.geOpsEnabled;

public class DecideRestockNode implements ExecutableNode {
    private Script script;
    private CombinationRecipes recipe;
    private boolean isJumping = false;
    private Class<? extends ExecutableNode> jumpTarget;
    private List<Edge> adjNodes;
    private Margins margin;

    private int unfCountMinThreshold;
    private long lastIntermittentBuyTime = 0;

    public DecideRestockNode(Script script) {
        this.script = script;
        margin = Margins.getInstance(script);
        this.recipe = margin.getCurrentRecipe();

        adjNodes = Arrays.asList(
                new Edge(Withdraw10Primary.class, 1),
                new Edge(Withdraw14Primary.class, 100),
                new Edge(WithdrawXPrimary.class, 1),
                new Edge(Withdraw10Secondary.class, 1),
                new Edge(Withdraw14Secondary.class, 100),
                new Edge(WithdrawXSecondary.class, 1));

        unfCountMinThreshold = ThreadLocalRandom.current().nextInt(300, 600);
        script.log(IntermittentSell.class.getSimpleName() + " runs when there are " + unfCountMinThreshold + " unf potions");
    }

    @Override
    public boolean canExecute() {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
        return script.getBank().isOpen();
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        Bank bank = script.getBank();
        int primaryRemaining = 0;

        if(recipe != null){
            primaryRemaining = (int) bank.getAmount(recipe.getPrimaryItemName());
            int secondaryRemaining = (int) bank.getAmount(recipe.getSecondaryItemName());
            if(secondaryRemaining < 14 || (!recipe.isUnfPotion() && primaryRemaining < 14)){
                script.stop(false);
                script.log("ran out of secondary or primary or is not doing unf pots");
                //TODO: buy vials
            }
            if(recipe.isUnfPotion()){
                jumpTarget = null;
                if(isBreakImminent()){
                    while(isBreakImminent()){
                        script.log("sleeping until break handler takes over");
                        MethodProvider.sleep(5000);
                    }
                } else {
                    if(recipe == null && geOpsEnabled){
                        isJumping = true;
                        jumpTarget = InitialBuy.class;
                    }
                    else if(primaryRemaining < 14){
                        if(recipe.isUnfPotion()){
                            if(geOpsEnabled){
                                isJumping = true;
                                jumpTarget = AbortRelevantOffers.class;
                            } else {
                                CombinationRecipes next = null;
                                for(CombinationRecipes recipes: CombinationRecipes.values()){
                                    if(bank.getAmount(recipes.getPrimaryItemName()) >= 14){
                                        next = recipes;
                                    }
                                }
                                if(next == null){
                                    script.log("all cleaned up!");
                                    script.stop(false);
                                } else {
                                    script.log("selected " + next + " as next");
                                    margin.setCurrentRecipe(next);
                                }
                            }
                        }
                    } else if(isJumpingToIntermittentSell() && geOpsEnabled){
                        isJumping = true;
                        jumpTarget = IntermittentSell.class;
                    } else if(isJumpingToIntermittentBuy() && geOpsEnabled){
                        isJumping = true;
                        jumpTarget = IntermittentBuy.class;
                    }
                    if(jumpTarget != null)
                        script.log("jumping to: " + jumpTarget.getSimpleName());
                }
            }
        }


        return 0;
    }

    private boolean isBreakImminent(){
        RandomExecutor randomExecutor = script.getBot().getRandomExecutor();
        int minsUntilBreak = randomExecutor != null ? randomExecutor.getTimeUntilBreak() : -1;
        if(minsUntilBreak < 3){
            script.log("Break Imminent in: " + minsUntilBreak + " mins");
        }
        return minsUntilBreak < 2 && minsUntilBreak >= 0;
    }



    private boolean isJumpingToIntermittentSell(){
        long unfCount = script.getBank().getAmount(recipe.getFinishedItemName()) + script.getInventory().getAmount(recipe.getFinishedItemName());
        if(unfCount > unfCountMinThreshold){
            unfCountMinThreshold = ThreadLocalRandom.current().nextInt(200, 400);
            script.log(IntermittentSell.class.getSimpleName() + " will run again when there are " + unfCountMinThreshold + " or more unf potions");
            return true;
        }
        return false;
    }

    private boolean isJumpingToIntermittentBuy(){
        long nowUnix = Instant.now().getEpochSecond();
        long timeSinceLast = nowUnix - lastIntermittentBuyTime;
        GrandExchange.Box finishedProductSellBox = findFinishedProductSellingBox();
        if(finishedProductSellBox != null && timeSinceLast > 600){
            GrandExchange ge = script.getGrandExchange();
            int amountTraded = ge.getAmountTraded(finishedProductSellBox);
            int totalAmountToTrade = ge.getAmountToTransfer(finishedProductSellBox);
            double completionPercent = amountTraded / totalAmountToTrade;
            if(completionPercent >= 0.5){
                lastIntermittentBuyTime = nowUnix;
                return true;
            }
        }
        return false;
    }

    private GrandExchange.Box findPrimaryIngredientBuyingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(script.getGrandExchange().getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    private GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(script.getGrandExchange().getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
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
        return jumpTarget;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
