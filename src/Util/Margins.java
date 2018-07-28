package Util;

import Util.GrandExchangeUtil.GrandExchangeOperations;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import java.util.Arrays;
import java.util.HashMap;
/*
Singleton class that stores sell/buy prices of ingredients and their final products
*/

public class Margins {
    private static Margins singleton;
    private boolean doInitialMarginCheck = true;
    private Script script;
    private HashMap<CombinationRecipes, MarginPrices> priceData;
    private GrandExchangeOperations operations;
    private CombinationRecipes currentRecipe;

    public static int switchRecipeIfLower = 100;
    public static final int[] DEFAULT_MARGIN = {100000, 100000};

    private Margins(Script script){
        this.script = script;
        operations = GrandExchangeOperations.getInstance(script.bot);
        priceData = new HashMap<>();
        priceData.put(CombinationRecipes.AVANTOE, new MarginPrices());
        priceData.put(CombinationRecipes.TOADFLAX, new MarginPrices());
        priceData.put(CombinationRecipes.IRIT, new MarginPrices());
        priceData.put(CombinationRecipes.KWUARM, new MarginPrices());
        priceData.put(CombinationRecipes.HARRALANDER, new MarginPrices());
    }

    public static Margins getInstance(Script script){
        if(singleton == null){
            singleton = new Margins(script);
        }
        return singleton;
    }

    public CombinationRecipes findAndSetNextRecipe() throws InterruptedException {
        int profitMargin = 0;
        if(currentRecipe != null)
            profitMargin = priceData.get(currentRecipe).getConversionProfit();
        else
            doInitialMarginCheck = true;
        if(doInitialMarginCheck || profitMargin < switchRecipeIfLower){
            doInitialMarginCheck = false;
            currentRecipe = findAllConversionMargins(); //a new recipe is found here if the currentRecipe is poor
        } else if(priceData.get(currentRecipe).getSecondsSinceLastUpdate() > 600){
            profitMargin = priceData.get(currentRecipe).getConversionProfit();
            if(profitMargin < switchRecipeIfLower)
                currentRecipe = findAllConversionMargins();
        }
        if(currentRecipe == null){
            script.stop(false);
            throw new NullPointerException("findAndSetNextRecipe(): nextRecipe is null");
        }
        return currentRecipe;
    }

    public static void markSingletonAsNull(){
        singleton = null;
    }

    public int[] findSpecificConversionMargin(CombinationRecipes recipe) throws InterruptedException {
        script.log("finding conversion margin for: " + recipe.name());
        int[] primaryMargin = findPrimaryIngredientMargin(recipe);

        if(marginNotDefault(primaryMargin)){
            int[] productMargin = findFinishedProductMargin(recipe);
            if(marginNotDefault(productMargin)){
                updatePrimaryMargin(recipe, primaryMargin);
                updateProductMargin(recipe, productMargin);
                return priceData.get(recipe).getConversionMargin();
            }
        }
        script.warn("failed to find margin for " + recipe + " invalidating for this session");
        priceData.remove(recipe);
        return DEFAULT_MARGIN;
    }

    public CombinationRecipes findAllConversionMargins() throws InterruptedException {
        int secondBestDeltaMargin = -1;
        int bestDeltaMargin = 0;
        int herbLvl = script.skills.getDynamic(Skill.HERBLORE);
        CombinationRecipes best = null;
        for(CombinationRecipes conv : priceData.keySet()){
            if(herbLvl < conv.getReqLvl()){
                script.log("do not have req lvl for " + conv + " conversion, skipping.");
                continue;
            }
            MarginPrices prices = priceData.get(conv);
            if(prices.getSecondsSinceLastUpdate() > 180){
                findSpecificConversionMargin(conv);
            } else {
                script.log("last price check for " + conv.getPrimaryItemName() + " conversion was less than 2 mins ago. Using cached margin");
                prices.getConversionMargin();
            }
            int marginDelta = prices.getConversionProfit();
            if(marginDelta > bestDeltaMargin && marginDelta <= 1000){ //if over 1000 something likely went wrong with pricecheck
                secondBestDeltaMargin = bestDeltaMargin;
                bestDeltaMargin = marginDelta;
                best = conv;
                if(marginDelta >= 200){
                    script.log(conv + " has at least a 200 margin, settling with that");
                    break;
                }
            } else if(marginDelta > secondBestDeltaMargin){
                secondBestDeltaMargin = marginDelta;
            }
        }
        if(best != null){
            if(bestDeltaMargin <= 50){
                script.log("all margins are 50 or less, stopping");
                script.stop(true);
            } else {
                script.log(best.name() + " has best delta margin at: " + bestDeltaMargin + " with margin: " + Arrays.toString(priceData.get(best).getConversionMargin()));
                switchRecipeIfLower = 100 > secondBestDeltaMargin ? 100 : secondBestDeltaMargin;
                script.log("switching recipes if this recipe margin is lower than " + switchRecipeIfLower);
            }
        } else {
            script.log("price check all operation failed: best is null!");
            script.stop(false);
        }
        return best;
    }

    public int[] findFinishedProductMargin(CombinationRecipes product) throws InterruptedException {
        int[] productMargin = operations.priceCheckItemMargin(product.getFinishedItemID(), product.getGeSearchTerm());
        int attempts = 0;
        while((productMargin[0] == DEFAULT_MARGIN[0] || productMargin[1] == DEFAULT_MARGIN[1]) && attempts < 3){
            attempts++;

            operations.abortOffersWithItem(product.getFinishedItemName());
            productMargin = operations.priceCheckItemMargin(product.getFinishedItemID(), product.getGeSearchTerm());
        }
        if(attempts >= 3){
            operations.collect();
            return DEFAULT_MARGIN;
        }
        MarginPrices prices = priceData.get(product);
        prices.setProductInstantBuy(productMargin[1]);
        prices.setProductInstantSell(productMargin[0]);
        return productMargin;
    }

    public int[] findPrimaryIngredientMargin(CombinationRecipes primary) throws InterruptedException {
        int[] primaryMargin = operations.priceCheckItemMargin(primary.getPrimaryItemID(), primary.getGeSearchTerm());
        int attempts = 0;
        while((primaryMargin[0] == DEFAULT_MARGIN[0] || primaryMargin[1] == DEFAULT_MARGIN[1]) && attempts < 3){
            attempts++;
            operations.abortOffersWithItem(primary.getPrimaryItemName());
            primaryMargin = operations.priceCheckItemMargin(primary.getPrimaryItemID(), primary.getGeSearchTerm());
        }
        if(attempts >= 3){
            operations.collect();
            return DEFAULT_MARGIN;
        }
        updatePrimaryMargin(primary, primaryMargin);
        return primaryMargin;
    }

    public int[] getCachedConversionMargin(CombinationRecipes recipe) {
        if(priceData.containsKey(recipe))
            return priceData.get(recipe).getConversionMargin();
        return DEFAULT_MARGIN;
    }

    public int[] getCachedFinishedProductMargin(CombinationRecipes product){
        MarginPrices prices = priceData.get(product);
        return new int[]{prices.getProductInstantSell(), prices.getProductInstantBuy()};
    }

    public int[] getCachedPrimaryIngredientMargin(CombinationRecipes primary) {
        MarginPrices prices = priceData.get(primary);
        return new int[]{prices.getPrimaryInstantSell(), prices.getPrimaryInstantBuy()};
    }

    public CombinationRecipes getCurrentRecipe() {
        return currentRecipe;
    }

    public void setCurrentRecipe(CombinationRecipes currentRecipe) {
        if(currentRecipe != null){
            script.log("recipe set to: " + currentRecipe.name());
        } else {
            script.log("passed argument is null");
        }

        this.currentRecipe = currentRecipe;
    }

    private boolean marginNotDefault(int[] margin){
        return margin[0] != DEFAULT_MARGIN[0] && margin[1] != DEFAULT_MARGIN[1];
    }

    private void updatePrimaryMargin(CombinationRecipes recipe, int[] margin){
        MarginPrices prices = priceData.get(recipe);
        prices.setPrimaryInstantBuy(margin[1]);
        prices.setPrimaryInstantSell(margin[0]);
        prices.setLastUpdate();
    }

    private void updateProductMargin(CombinationRecipes recipe, int[] margin){
        MarginPrices prices = priceData.get(recipe);
        prices.setProductInstantBuy(margin[1]);
        prices.setProductInstantSell(margin[0]);
        prices.setLastUpdate();
    }
}
