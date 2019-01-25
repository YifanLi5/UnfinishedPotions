package Nodes.BankingNodes;

import Nodes.BankingNodes.PrimaryWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.PrimaryWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.PrimaryWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.WithdrawXSecondary;
import Nodes.GENodes.AbortRelevantOffers;
import Nodes.GENodes.InitialBuyWaitUntil;
import Nodes.GENodes.IntermittentBuy;
import Nodes.GENodes.IntermittentSell;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.event.RandomExecutor;
import org.osbot.rs07.script.MethodProvider;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.ScriptPaint.geOpsEnabled;

public class DecideRestockNode extends MethodProvider implements ExecutableNode {

    private CombinationRecipes recipe;
    private boolean isJumping = false;
    private Class<? extends ExecutableNode> jumpTarget;
    private List<Edge> adjNodes;
    private Margins margin;

    private int unfCountMinThreshold;
    private long lastIntermittentBuyTime = 0;

    public DecideRestockNode(Bot bot) {
        exchangeContext(bot);
        margin = Margins.getInstance(bot);
        this.recipe = margin.getCurrentRecipe();

        adjNodes = Arrays.asList(
                new Edge(Withdraw10Primary.class, 1),
                new Edge(Withdraw14Primary.class, 100),
                new Edge(WithdrawXPrimary.class, 1),
                new Edge(Withdraw10Secondary.class, 1),
                new Edge(Withdraw14Secondary.class, 100),
                new Edge(WithdrawXSecondary.class, 1));

        unfCountMinThreshold = ThreadLocalRandom.current().nextInt(300, 600);
        log(IntermittentSell.class.getSimpleName() + " runs when there are " + unfCountMinThreshold + " unf potions");
    }

    @Override
    public boolean canExecute() {
        return recipe.canUseGE() && geOpsEnabled && bank.isOpen();
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        if(recipe != null){
            int primaryRemaining = (int) bank.getAmount(recipe.getPrimary());
            int secondaryRemaining = (int) bank.getAmount(recipe.getSecondary());

            //TODO: support restock of secondary
            if(secondaryRemaining < 14){
                return -1;
            }

            jumpTarget = null;
            if(isBreakImminent()){
                while(isBreakImminent()){
                    log("sleeping until break handler takes over");
                    sleep(10000);
                }
            } else {
                if(recipe == null && geOpsEnabled){
                    isJumping = true;
                    jumpTarget = InitialBuyWaitUntil.class;
                }
                else if(primaryRemaining < 14){
                    if(geOpsEnabled){
                        isJumping = true;
                        jumpTarget = AbortRelevantOffers.class;
                    } else {
                        //geOpsEnabled = false means time to clean up! Go through every clean herb and make unf potions.
                        //when done, stop script.
                        CombinationRecipes next = null;
                        for(CombinationRecipes recipes: CombinationRecipes.values()){
                            if(bank.contains(recipes.getPrimary())){
                                next = recipes;
                                break;
                            }
                        }
                        if(next == null){
                            log("all cleaned up!");
                            return -1;
                        } else {
                            log("selected " + next + " as next");
                            margin.setCurrentRecipe(next);
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
                    log("jumping to: " + jumpTarget.getSimpleName());
            }
        }
        return 0;
    }

    private boolean isBreakImminent(){
        RandomExecutor randomExecutor = bot.getRandomExecutor();
        int minsUntilBreak = randomExecutor != null ? randomExecutor.getTimeUntilBreak() : -1;
        if(minsUntilBreak < 3){
            log("Break Imminent in: " + minsUntilBreak + " mins");
        }
        return minsUntilBreak < 2 && minsUntilBreak >= 0;
    }



    private boolean isJumpingToIntermittentSell(){
        long unfCount = bank.getAmount(recipe.getProduct()) + inventory.getAmount(recipe.getProduct());
        if(unfCount > unfCountMinThreshold){
            unfCountMinThreshold = ThreadLocalRandom.current().nextInt(100, 400);
            log(IntermittentSell.class.getSimpleName() + " will run again when there are " + unfCountMinThreshold + " or more unf potions");
            return true;
        }
        return false;
    }

    //do intermittentBuy if the offer completion percent of an existing potion sell offer exceeds 50%
    //and the last time intermittentBuy ran was over 10mins ago
    private boolean isJumpingToIntermittentBuy(){
        long nowUnix = Instant.now().getEpochSecond();
        long timeSinceLast = nowUnix - lastIntermittentBuyTime;
        GrandExchange.Box finishedProductSellBox = findFinishedProductSellingBox();
        if(finishedProductSellBox != null && timeSinceLast > 600){
            int amountTraded = grandExchange.getAmountTraded(finishedProductSellBox);
            int totalAmountToTrade = grandExchange.getAmountToTransfer(finishedProductSellBox);
            double completionPercent = amountTraded / totalAmountToTrade;
            if(completionPercent >= 0.5){
                lastIntermittentBuyTime = nowUnix;
                return true;
            }
        }
        return false;
    }

    private GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(grandExchange.getItemId(box) == recipe.getProduct().getId()){
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
        log(this.getClass().getSimpleName());
    }
}
