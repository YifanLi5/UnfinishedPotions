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
    private boolean doInitialMarginCheck = true;
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

        if(marginNotDefault(primaryMargin)){
            int[] productMargin = findFinishedProductMargin(recipe);
            if(marginNotDefault(productMargin)){
                updatePrimaryMargin(recipe, primaryMargin);
                updateProductMargin(recipe, productMargin);
                return priceData.get(recipe).getConversionMargin();
            }
        }
        warn("failed to find margin for " + recipe + " invalidating for this session");
        priceData.remove(recipe);
        return DEFAULT_MARGIN;
    }

    public CombinationRecipes findAllConversionMargins() throws InterruptedException {
        int secondBestDeltaMargin = -1;
        int bestDeltaMargin = 0;
        CombinationRecipes best = null;
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
            if(marginDelta > bestDeltaMargin && marginDelta <= 1000){ //if over 1000 something likely went wrong with pricecheck
                secondBestDeltaMargin = bestDeltaMargin;
                bestDeltaMargin = marginDelta;
                best = recipe;
                if(marginDelta >= 200){
                    log(recipe + " has at least a 200 margin, settling with that");
                    break;
                }
            } else if(marginDelta > secondBestDeltaMargin){
                secondBestDeltaMargin = marginDelta;
            }
        }
        log(best + " has best delta margin at: " + bestDeltaMargin + " with margin: " + Arrays.toString(priceData.get(best).getConversionMargin()));
        switchRecipeIfLower = 100 > secondBestDeltaMargin ? 100 : secondBestDeltaMargin;
        log("switching recipes if this recipe margin is lower than " + switchRecipeIfLower);
        return best;
    }

    public int[] findFinishedProductMargin(CombinationRecipes recipe) throws InterruptedException {
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
        MarginPrices prices = priceData.get(recipe);
        prices.setProductInstantBuy(productMargin[1]);
        prices.setProductInstantSell(productMargin[0]);
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
