package Util;

import Util.GrandExchangeUtil.GrandExchangeOperations;
import org.osbot.rs07.script.Script;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

public class ConversionMargins {
    private static ConversionMargins singleton;
    private Script script;
    private HashMap<UnfPotionRecipes, int[]> marginsDB;
    private HashMap<UnfPotionRecipes, Instant> lastUpdateTimestamps;
    private GrandExchangeOperations operations;
    private UnfPotionRecipes currentRecipe = UnfPotionRecipes.IRIT;

    private ConversionMargins(Script script){
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
    }

    public static ConversionMargins getInstance(Script script){
        if(singleton == null){
            singleton = new ConversionMargins(script);
        }
        return singleton;
    }

    public int[] priceCheckSpecific(UnfPotionRecipes unfPotionRecipes) throws InterruptedException {
        int[] primaryMargin = operations.priceCheckItem(unfPotionRecipes.getPrimaryItemID(), unfPotionRecipes.getGeSearchTerm());
        int[] finishedMargin = operations.priceCheckItem(unfPotionRecipes.getFinishedItemID(), unfPotionRecipes.getGeSearchTerm());
        int[] margin = {primaryMargin[1], finishedMargin[0]};
        script.log(unfPotionRecipes.name() + " unf potion conversion has margin: " + Arrays.toString(margin));
        return margin;
    }

    public UnfPotionRecipes priceCheckAll() throws InterruptedException {
        int bestDeltaMargin = 0;
        UnfPotionRecipes best = UnfPotionRecipes.CLAY;
        for(UnfPotionRecipes conv : marginsDB.keySet()){
            script.log("finding conversion margin for: " + conv.name());
            int[] primaryMargin = operations.priceCheckItem(conv.getPrimaryItemID(), conv.getGeSearchTerm());
            int[] finishedMargin = operations.priceCheckItem(conv.getFinishedItemID(), conv.getGeSearchTerm());
            int[] convMargin = {primaryMargin[1], finishedMargin[0]};
            marginsDB.replace(conv, convMargin);
            script.log(conv.name() + " unf potion conversion has margin: " + Arrays.toString(convMargin) + " delta: " + (convMargin[1] - convMargin[0]));
            int marginDelta = convMargin[1] - convMargin[0];
            if(marginDelta > bestDeltaMargin){
                bestDeltaMargin = marginDelta;
                best = conv;
            }
            lastUpdateTimestamps.put(conv, Instant.now());
        }
        if(best != UnfPotionRecipes.CLAY){
            script.log(best.name() + " has best delta margin at: " + bestDeltaMargin + " with margin: " + Arrays.toString(marginsDB.get(best)));
        } else {
            script.log("price check all operation failed");
            return UnfPotionRecipes.IRIT;
        }
        return best;
    }

    public int getSecondsSinceLastUpdate(UnfPotionRecipes unfPotionRecipes) {
        Instant lastUpdate = lastUpdateTimestamps.get(unfPotionRecipes);
        if(lastUpdate != null){
            return (int) (Instant.now().getEpochSecond() - lastUpdate.getEpochSecond());
        }
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
