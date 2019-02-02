package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Util.CombinationRecipes;
import org.osbot.rs07.Bot;

import java.util.Collections;
import java.util.List;

public class InitialBuy extends Buy {

    public InitialBuy(Bot bot) {
        super(bot);
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(withdrawCashLeave10k() && inventory.getAmount(995) >= 300000){
            CombinationRecipes recipe = margins.findAllConversionMargins();
            margins.setCurrentRecipe(recipe);
            int[] margin = margins.getCachedPrimaryIngredientMargin(recipe);
            if(operations.buyUpToLimit(recipe.getPrimary(), margin[1], 1000)){
                waitForBuy(recipe);
                if(operations.collect()){
                    log("collected bought items");
                    return 1000;
                }
            }

        } else{
            log("not enough gp, need at least 300k in bank");
            bot.getScriptExecutor().stop(false);
        }
        return 0;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(DepositNode.class, 1));
    }

}
