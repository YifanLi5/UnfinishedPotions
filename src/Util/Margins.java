package Util;

import Util.GrandExchangeUtil.GrandExchangeOperations;
import org.osbot.rs07.script.Script;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

public class Margins {
    private static Margins singleton;
    private Script script;
    private HashMap<UnfPotionRecipes, int[]> marginsDB;
    private HashMap<UnfPotionRecipes, Instant> lastUpdateTimestamps;
    private HashMap<UnfPotionRecipes, int[]> primaryIngredientMargins;
    private HashMap<UnfPotionRecipes, int[]> finishedProductMargins;
    private GrandExchangeOperations operations;
    private UnfPotionRecipes currentRecipe;

    public static final int SWITCH_RECIPE_IF_LOWER = 150;

    private Margins(Script script){
        this.script = script;
        operations = GrandExchangeOperations.getInstance(script.bot);
        marginsDB = new HashMap<>();
        marginsDB.put(UnfPotionRecipes.AVANTOE, null);
        marginsDB.put(UnfPotionRecipes.TOADFLAX, null);
        marginsDB.put(UnfPotionRecipes.IRIT, null);
        marginsDB.put(UnfPotionRecipes.KWUARM, null);

        lastUpdateTimestamps = new HashMap<>();
        lastUpdateTimestamps.put(UnfPotionRecipes.AVANTOE, null);
        lastUpdateTimestamps.put(UnfPotionRecipes.TOADFLAX, null);
        lastUpdateTimestamps.put(UnfPotionRecipes.IRIT, null);
        lastUpdateTimestamps.put(UnfPotionRecipes.KWUARM, null);

        primaryIngredientMargins = new HashMap<>();
        primaryIngredientMargins.put(UnfPotionRecipes.AVANTOE, null);
        primaryIngredientMargins.put(UnfPotionRecipes.TOADFLAX, null);
        primaryIngredientMargins.put(UnfPotionRecipes.IRIT, null);
        primaryIngredientMargins.put(UnfPotionRecipes.KWUARM, null);

        finishedProductMargins = new HashMap<>();
        finishedProductMargins.put(UnfPotionRecipes.AVANTOE, null);
        finishedProductMargins.put(UnfPotionRecipes.TOADFLAX, null);
        finishedProductMargins.put(UnfPotionRecipes.IRIT, null);
        finishedProductMargins.put(UnfPotionRecipes.KWUARM, null);
    }

    public static Margins getInstance(Script script){
        if(singleton == null){
            singleton = new Margins(script);
        }
        return singleton;
    }

    public int[] findFinishedProductMargin(UnfPotionRecipes product) throws InterruptedException {
        int[] productMargin = operations.priceCheckItemMargin(product.getFinishedItemID(), product.getGeSearchTerm());
        finishedProductMargins.put(product, productMargin);
        return productMargin;
    }

    public int[] getFinishedProductMargin(UnfPotionRecipes product){
        return finishedProductMargins.get(product);
    }

    public int[] findPrimaryIngredientMargin(UnfPotionRecipes primary) throws InterruptedException {
        int[] primaryMargin = operations.priceCheckItemMargin(primary.getPrimaryItemID(), primary.getGeSearchTerm());
        primaryIngredientMargins.put(primary, primaryMargin);
        return primaryMargin;
    }

    public int[] getCachedPrimaryIngredientMargin(UnfPotionRecipes primary) {
        return primaryIngredientMargins.get(primary);
    }

    public int[] findSpecificConversionMargin(UnfPotionRecipes unfPotionRecipes) throws InterruptedException {
        script.log("finding conversion margin for: " + unfPotionRecipes.name());
        int[] primaryMargin = operations.priceCheckItemMargin(unfPotionRecipes.getPrimaryItemID(), unfPotionRecipes.getGeSearchTerm());
        int[] finishedProductMargin = operations.priceCheckItemMargin(unfPotionRecipes.getFinishedItemID(), unfPotionRecipes.getGeSearchTerm());
        int[] conversionMargin = {primaryMargin[1], finishedProductMargin[0]};

        primaryIngredientMargins.replace(unfPotionRecipes, primaryMargin);
        marginsDB.replace(unfPotionRecipes, conversionMargin);
        lastUpdateTimestamps.replace(unfPotionRecipes, Instant.now());
        script.log(unfPotionRecipes.name() + " unf potion conversion has margin: " + Arrays.toString(conversionMargin) + " delta: " + (conversionMargin[1] - conversionMargin[0]));
        return conversionMargin;
    }

    public UnfPotionRecipes findAllConversionMargins() throws InterruptedException {
        int bestDeltaMargin = 0;
        UnfPotionRecipes best = null;
        for(UnfPotionRecipes conv : marginsDB.keySet()){
            int[] conversionMargin = findSpecificConversionMargin(conv);
            int marginDelta = conversionMargin[1] - conversionMargin[0];
            if(marginDelta > bestDeltaMargin){
                bestDeltaMargin = marginDelta;
                best = conv;
            }
        }
        if(best != null){
            script.log(best.name() + " has best delta margin at: " + bestDeltaMargin + " with margin: " + Arrays.toString(marginsDB.get(best)));
        } else {
            script.log("price check all operation failed, setting recipe to default(IRIT)");
            return UnfPotionRecipes.IRIT;
        }
        return best;
    }

    public void updateConversionMarginEntry(UnfPotionRecipes recipe, int[] newMargin){
        marginsDB.put(recipe, newMargin);
        lastUpdateTimestamps.put(recipe, Instant.now());
    }

    public void updatePrimaryIngredientBuyPrice(UnfPotionRecipes recipe, int newPrice){
        int[] oldMargin = marginsDB.get(recipe);
        marginsDB.put(recipe, new int[]{newPrice, oldMargin[1]});
    }

    public void updateFinishedProductSellPrice(UnfPotionRecipes recipe, int newPrice){
        int[] oldMargin = marginsDB.get(recipe);
        marginsDB.put(recipe, new int[]{oldMargin[0], newPrice});
    }

    public int getSecondsSinceLastUpdate(UnfPotionRecipes unfPotionRecipes) {
        Instant lastUpdate = lastUpdateTimestamps.get(unfPotionRecipes);
        if(lastUpdate != null){
            int secondsAgo = (int) (Instant.now().getEpochSecond() - lastUpdate.getEpochSecond());
            script.log("last update was: " + secondsAgo + " ago");
            return secondsAgo;
        }
        script.log("there was no last update");
        return Integer.MAX_VALUE;
    }

    public UnfPotionRecipes getBestMarginRecipe(){
        return marginsDB.entrySet().stream().max((a, b) -> {
            int[] marginA = a.getValue();
            int[] marginB = b.getValue();
            int deltaA = marginA[1] - marginA[0];
            int deltaB = marginB[1] - marginB[0];
            return deltaA > deltaB ? deltaA : deltaB;
        }).get().getKey();
    }

    public int[] getMargin(UnfPotionRecipes unfPotionRecipes) {
        int[] margin = marginsDB.get(unfPotionRecipes);
        if(margin != null){
            return margin;
        }
        return new int[2];
    }

    public int[] getMarginOfCurrentRecipe(){
        return getMargin(currentRecipe);
    }

    public UnfPotionRecipes getCurrentRecipe() {
        return currentRecipe;
    }

    public void setCurrentRecipe(UnfPotionRecipes currentRecipe) {
        script.log("recipe set to: " + currentRecipe.name());
        this.currentRecipe = currentRecipe;
    }
}
