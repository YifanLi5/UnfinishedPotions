package Util;

import Util.GrandExchangeUtil.GrandExchangeOperations;
import org.osbot.rs07.script.Script;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

public class Margins {
    private static Margins singleton;
    private Script script;
    private HashMap<ItemCombinationRecipes, int[]> conversionMargins;
    private HashMap<ItemCombinationRecipes, Instant> lastUpdateTimestamps;
    private HashMap<ItemCombinationRecipes, int[]> primaryIngredientMargins;
    private HashMap<ItemCombinationRecipes, int[]> finishedProductMargins;
    private GrandExchangeOperations operations;
    private ItemCombinationRecipes currentRecipe;

    public static final int SWITCH_RECIPE_IF_LOWER = 150;

    private Margins(Script script){
        this.script = script;
        operations = GrandExchangeOperations.getInstance(script.bot);
        conversionMargins = new HashMap<>();
        conversionMargins.put(ItemCombinationRecipes.AVANTOE, new int[]{100000, -100000});
        conversionMargins.put(ItemCombinationRecipes.TOADFLAX, new int[]{100000, -100000});
        conversionMargins.put(ItemCombinationRecipes.IRIT, new int[]{100000, -100000});
        conversionMargins.put(ItemCombinationRecipes.KWUARM, new int[]{100000, -100000});

        lastUpdateTimestamps = new HashMap<>();
        lastUpdateTimestamps.put(ItemCombinationRecipes.AVANTOE, null);
        lastUpdateTimestamps.put(ItemCombinationRecipes.TOADFLAX, null);
        lastUpdateTimestamps.put(ItemCombinationRecipes.IRIT, null);
        lastUpdateTimestamps.put(ItemCombinationRecipes.KWUARM, null);

        primaryIngredientMargins = new HashMap<>();
        primaryIngredientMargins.put(ItemCombinationRecipes.AVANTOE, new int[]{100000, -100000});
        primaryIngredientMargins.put(ItemCombinationRecipes.TOADFLAX, new int[]{100000, -100000});
        primaryIngredientMargins.put(ItemCombinationRecipes.IRIT, new int[]{100000, -100000});
        primaryIngredientMargins.put(ItemCombinationRecipes.KWUARM, new int[]{100000, -100000});

        finishedProductMargins = new HashMap<>();
        finishedProductMargins.put(ItemCombinationRecipes.AVANTOE, new int[]{100000, -100000});
        finishedProductMargins.put(ItemCombinationRecipes.TOADFLAX, new int[]{100000, -100000});
        finishedProductMargins.put(ItemCombinationRecipes.IRIT, new int[]{100000, -100000});
        finishedProductMargins.put(ItemCombinationRecipes.KWUARM, new int[]{100000, -100000});
    }

    public static Margins getInstance(Script script){
        if(singleton == null){
            singleton = new Margins(script);
        }
        return singleton;
    }

    public int[] findFinishedProductMargin(ItemCombinationRecipes product) throws InterruptedException {
        int[] productMargin = operations.priceCheckItemMargin(product.getFinishedItemID(), product.getGeSearchTerm());
        finishedProductMargins.put(product, productMargin);
        return productMargin;
    }

    public int[] getFinishedProductMargin(ItemCombinationRecipes product){
        return finishedProductMargins.get(product);
    }

    public int[] findPrimaryIngredientMargin(ItemCombinationRecipes primary) throws InterruptedException {
        int[] primaryMargin = operations.priceCheckItemMargin(primary.getPrimaryItemID(), primary.getGeSearchTerm());
        primaryIngredientMargins.put(primary, primaryMargin);
        return primaryMargin;
    }

    public int[] getCachedPrimaryIngredientMargin(ItemCombinationRecipes primary) {
        return primaryIngredientMargins.get(primary);
    }

    public int[] findSpecificConversionMargin(ItemCombinationRecipes itemCombinationRecipes) throws InterruptedException {
        script.log("finding conversion margin for: " + itemCombinationRecipes.name());
        int[] primaryMargin = operations.priceCheckItemMargin(itemCombinationRecipes.getPrimaryItemID(), itemCombinationRecipes.getGeSearchTerm());
        int[] finishedProductMargin = operations.priceCheckItemMargin(itemCombinationRecipes.getFinishedItemID(), itemCombinationRecipes.getGeSearchTerm());
        int[] conversionMargin = {primaryMargin[1], finishedProductMargin[0]};

        primaryIngredientMargins.replace(itemCombinationRecipes, primaryMargin);
        conversionMargins.replace(itemCombinationRecipes, conversionMargin);
        lastUpdateTimestamps.replace(itemCombinationRecipes, Instant.now());
        script.log(itemCombinationRecipes.name() + " unf potion conversion has margin: " + Arrays.toString(conversionMargin) + " delta: " + (conversionMargin[1] - conversionMargin[0]));
        return conversionMargin;
    }

    public ItemCombinationRecipes findAllConversionMargins() throws InterruptedException {
        int bestDeltaMargin = 0;
        ItemCombinationRecipes best = null;
        for(ItemCombinationRecipes conv : conversionMargins.keySet()){
            int[] conversionMargin = findSpecificConversionMargin(conv);
            int marginDelta = conversionMargin[1] - conversionMargin[0];
            if(marginDelta > bestDeltaMargin){
                bestDeltaMargin = marginDelta;
                best = conv;
            }
        }
        if(best != null){
            script.log(best.name() + " has best delta margin at: " + bestDeltaMargin + " with margin: " + Arrays.toString(conversionMargins.get(best)));
        } else {
            script.log("price check all operation failed, setting recipe to default(IRIT)");
            return ItemCombinationRecipes.IRIT;
        }
        return best;
    }

    public void updateConversionMarginEntry(ItemCombinationRecipes recipe, int[] newMargin){
        conversionMargins.put(recipe, newMargin);
        lastUpdateTimestamps.put(recipe, Instant.now());
    }

    public void updatePrimaryIngredientBuyPrice(ItemCombinationRecipes recipe, int newPrice){
        int[] oldMargin = conversionMargins.get(recipe);
        conversionMargins.put(recipe, new int[]{newPrice, oldMargin[1]});
    }

    public void updateFinishedProductSellPrice(ItemCombinationRecipes recipe, int newPrice){
        int[] oldMargin = conversionMargins.get(recipe);
        conversionMargins.put(recipe, new int[]{oldMargin[0], newPrice});
    }

    public int getSecondsSinceLastUpdate(ItemCombinationRecipes itemCombinationRecipes) {
        Instant lastUpdate = lastUpdateTimestamps.get(itemCombinationRecipes);
        if(lastUpdate != null){
            int secondsAgo = (int) (Instant.now().getEpochSecond() - lastUpdate.getEpochSecond());
            script.log("last update was: " + secondsAgo + " ago");
            return secondsAgo;
        }
        script.log("there was no last update");
        return Integer.MAX_VALUE;
    }

    public ItemCombinationRecipes getBestMarginRecipe(){
        return conversionMargins.entrySet().stream().max((a, b) -> {
            int[] marginA = a.getValue();
            int[] marginB = b.getValue();
            int deltaA = marginA[1] - marginA[0];
            int deltaB = marginB[1] - marginB[0];
            return deltaA > deltaB ? deltaA : deltaB;
        }).get().getKey();
    }

    public int[] getMargin(ItemCombinationRecipes itemCombinationRecipes) {
        int[] margin = conversionMargins.get(itemCombinationRecipes);
        if(margin != null){
            return margin;
        }
        return new int[2];
    }

    public int[] getMarginOfCurrentRecipe(){
        return getMargin(currentRecipe);
    }

    public ItemCombinationRecipes getCurrentRecipe() {
        return currentRecipe;
    }

    public void setCurrentRecipe(ItemCombinationRecipes currentRecipe) {
        script.log("recipe set to: " + currentRecipe.name());
        this.currentRecipe = currentRecipe;
    }
}
