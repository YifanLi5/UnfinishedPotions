package Util;

import Util.GrandExchangeUtil.GrandExchangeOperations;
import org.osbot.rs07.Bot;
import org.osbot.rs07.script.API;

import java.util.Arrays;
import java.util.HashMap;
/*
Singleton class that stores sell/buy prices of ingredients and their final products
*/

public class Margins extends API {
    private static Margins singleton;
    private HashMap<CombinationRecipes, MarginPrices> priceData;
    private GrandExchangeOperations operations;
    private CombinationRecipes currentRecipe;

    public static int switchRecipeIfLower = 100;
    public static final int[] DEFAULT_MARGIN = {100000, 100000};

    @Override
    public void initializeModule() {
        operations = GrandExchangeOperations.getInstance(this.bot);
        priceData = new HashMap<>();
        priceData.put(CombinationRecipes.AVANTOE_UNF_RECIPE, new MarginPrices());
        priceData.put(CombinationRecipes.TOADFLAX_UNF_RECIPE, new MarginPrices());
        priceData.put(CombinationRecipes.IRIT_UNF_RECIPE, new MarginPrices());
        priceData.put(CombinationRecipes.KWUARM_UNF_RECIPE, new MarginPrices());
        priceData.put(CombinationRecipes.HARRALANDER_UNF_RECIPE, new MarginPrices());
    }

    private Margins(){
    }

    public static Margins getInstance(Bot bot){
        if(singleton == null){
            singleton = new Margins();
            singleton.exchangeContext(bot);
            singleton.initializeModule();
        }
        return singleton;
    }

    public CombinationRecipes findAndSetNextRecipe() throws InterruptedException {
        int profitMargin = 0;
        if(currentRecipe != null)
            profitMargin = priceData.get(currentRecipe).getConversionProfit();

        //check if estimated profit margin is good enough. If it isn't, re-find all profit margins and pick the best
        if(profitMargin < switchRecipeIfLower){
            currentRecipe = findAllConversionMargins();
        }
        //check if the estimate profit margin is potentially outdated (> 10mins). If it is, re-find the profit margin
        //then check if it is still acceptable. re-find all profit margins if it is not.
        else if(priceData.get(currentRecipe).getSecondsSinceLastUpdate() > 600){
            findSpecificConversionMargin(currentRecipe);
            profitMargin = priceData.get(currentRecipe).getConversionProfit();
            if(profitMargin < switchRecipeIfLower)
                currentRecipe = findAllConversionMargins();
        }
        if(currentRecipe == null){
            throw new NullPointerException("findAndSetNextRecipe(): nextRecipe is null");
        }
        return currentRecipe;
    }

    public static void markSingletonAsNull(){
        singleton = null;
    }

    public int[] findSpecificConversionMargin(CombinationRecipes recipe) throws InterruptedException {
        log("finding conversion margin for: " + recipe.name());
        int[] primaryMargin = findPrimaryIngredientMargin(recipe);

        //If a margin check failed it returns an array [0,0]. This method simply checks if the check failed. (Is [0,0])
        if(marginIsValid(primaryMargin)){
            int[] productMargin = findFinishedProductMargin(recipe);
            if(marginIsValid(productMargin)){
                //update the pricing DB with the newly found information
                updatePrimaryMargin(recipe, primaryMargin);
                updateProductMargin(recipe, productMargin);
                return priceData.get(recipe).getConversionMargin();
            }
        }
        //A margin check can fail if it times out. This can happen if there is not enough market activity for this recipe.
        //In this case, don't bother doing this recipe for this session.
        warn("failed to find margin for " + recipe + " invalidating for this session");
        priceData.remove(recipe);
        return DEFAULT_MARGIN;
    }

    public CombinationRecipes findAllConversionMargins() throws InterruptedException {
        //keep track of the best and second best conversion profit margins.
        //The second best margin is used to set the switchIfLower variable.
        //If the best margin drops below this variable, it will trigger a recipe price check and potentially a recipe switch.
        int secondBestDeltaMargin = -1;
        int bestDeltaMargin = 0;
        CombinationRecipes bestRecipe = null;
        for(CombinationRecipes recipe : priceData.keySet()){
            if(skills.getDynamic(recipe.getSkill()) < recipe.getReqLvl()){
                log("do not have req lvl for " + recipe + " conversion, skipping.");
                continue;
            }
            MarginPrices prices = priceData.get(recipe);
            if(prices.getSecondsSinceLastUpdate() > 180){
                findSpecificConversionMargin(recipe);
            } else {
                log("last price check for " + recipe + " conversion was less than 2 mins ago. Using cached margin");
                prices.getConversionMargin();
            }
            int marginDelta = prices.getConversionProfit();
            //Note: margins over 1000 mean something went wrong with pricecheck. Random market shifts with can result in inaccurate long term pricing.
            if(marginDelta > bestDeltaMargin && marginDelta <= 1000){
                //A new best margin found! Update the variables.
                secondBestDeltaMargin = bestDeltaMargin;
                bestDeltaMargin = marginDelta;
                bestRecipe = recipe;
                //A recipe of 200 is considered great. Greedily accept it.
                if(marginDelta >= 200){
                    log(recipe + " has at least a 200 margin, settling with that");
                    break;
                }
            }

            else if(marginDelta > secondBestDeltaMargin){
                secondBestDeltaMargin = marginDelta;
            }
        }
        log(bestRecipe + " has best delta margin at: " + bestDeltaMargin + " with margin: " + Arrays.toString(priceData.get(bestRecipe).getConversionMargin()));
        switchRecipeIfLower = 100 > secondBestDeltaMargin ? 100 : secondBestDeltaMargin;
        log("switching recipes if this recipe margin is lower than " + switchRecipeIfLower);
        return bestRecipe;
    }

    public int[] findFinishedProductMargin(CombinationRecipes recipe) throws InterruptedException {
        //find the instant buy/sell margin of a unfinished potion.
        //This is NOT the recipe's profit margin. The recipe profit margin is found by...
        //profit margin = unf_potion instant sell - herb instant buy
        //AKA: profit margin = productMargin[0] - primaryMargin[1]
        int[] productMargin = operations.priceCheckItemMargin(recipe.getProduct());
        int attempts = 0;
        while((productMargin[0] == DEFAULT_MARGIN[0] || productMargin[1] == DEFAULT_MARGIN[1]) && attempts < 3){
            attempts++;
            operations.abortOffersWithItem(recipe.getProduct().getName());
            productMargin = operations.priceCheckItemMargin(recipe.getProduct());
        }
        if(attempts >= 3){
            operations.collect();
            return DEFAULT_MARGIN;
        }
        updateProductMargin(recipe, productMargin);
       /* MarginPrices prices = priceData.get(recipe);
        prices.setProductInstantBuy(productMargin[1]);
        prices.setProductInstantSell(productMargin[0]);*/
        return productMargin;
    }

    public int[] findPrimaryIngredientMargin(CombinationRecipes recipe) throws InterruptedException {
        int[] primaryMargin = operations.priceCheckItemMargin(recipe.getPrimary());
        int attempts = 0;
        while((primaryMargin[0] == DEFAULT_MARGIN[0] || primaryMargin[1] == DEFAULT_MARGIN[1]) && attempts < 3){
            attempts++;
            operations.abortOffersWithItem(recipe.getPrimary());
            primaryMargin = operations.priceCheckItemMargin(recipe.getPrimary());
        }
        if(attempts >= 3){
            operations.collect();
            return DEFAULT_MARGIN;
        }
        updatePrimaryMargin(recipe, primaryMargin);
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
            log("recipe set to: " + currentRecipe.name());
        } else {
            log("passed argument is null");
        }

        this.currentRecipe = currentRecipe;
    }

    private boolean marginIsValid(int[] margin){
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
