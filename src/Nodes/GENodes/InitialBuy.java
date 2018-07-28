package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Util.CombinationRecipes;
import org.osbot.rs07.script.Script;

import java.util.Collections;
import java.util.List;

public class InitialBuy extends GESpinLockBuyNode {

    public InitialBuy(Script script) {
        super(script);
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(withdrawCashLeave10k() && script.getInventory().getAmount(995) >= 300000){
            CombinationRecipes recipe = margins.findAllConversionMargins();
            margins.setCurrentRecipe(recipe);
            int[] margin = margins.getCachedPrimaryIngredientMargin(recipe);
            if(operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), margin[1], 1000)){
                waitForBuy();
                if(operations.collect()){
                    script.log("collected bought items");
                    return 1000;
                }
            }

        } else{
            script.log("not enough gp, need at least 300k in bank");
            script.stop(false);
        }
        return 0;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(DepositNode.class, 1));
    }

}
