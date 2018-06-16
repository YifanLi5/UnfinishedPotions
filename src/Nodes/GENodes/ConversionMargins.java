package Nodes.GENodes;

import Util.ComponentsEnum;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import org.osbot.rs07.script.Script;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

public class ConversionMargins {
    private static ConversionMargins singleton;
    private Script script;
    private HashMap<ComponentsEnum, int[]> marginsDB;
    private HashMap<ComponentsEnum, Instant> lastUpdateTimestamps;
    private GrandExchangeOperations operations;

    private ConversionMargins(Script script){
        this.script = script;
        operations = GrandExchangeOperations.getInstance(script.bot);
        marginsDB = new HashMap<>();
        marginsDB.put(ComponentsEnum.AVANTOE, null);
        marginsDB.put(ComponentsEnum.TOADFLAX, null);
        marginsDB.put(ComponentsEnum.IRIT, null);
        marginsDB.put(ComponentsEnum.KWUARM, null);
        lastUpdateTimestamps = new HashMap<>();
        lastUpdateTimestamps.put(ComponentsEnum.AVANTOE, null);
        lastUpdateTimestamps.put(ComponentsEnum.TOADFLAX, null);
        lastUpdateTimestamps.put(ComponentsEnum.IRIT, null);
        lastUpdateTimestamps.put(ComponentsEnum.KWUARM, null);
    }

    public static ConversionMargins getInstance(Script script){
        if(singleton == null){
            singleton = new ConversionMargins(script);
        }
        return singleton;
    }

    public int[] priceCheckSpecific(ComponentsEnum componentsEnum) throws InterruptedException {
        int[] primaryMargin = operations.priceCheckItem(componentsEnum.getPrimaryItemID(), componentsEnum.getGeSearchTerm());
        int[] finishedMargin = operations.priceCheckItem(componentsEnum.getFinishedItemID(), componentsEnum.getGeSearchTerm());

        return new int[]{primaryMargin[1], finishedMargin[0]};
    }

    public ComponentsEnum priceCheckAll() throws InterruptedException {
        int bestDeltaMargin = 0;
        ComponentsEnum best = ComponentsEnum.CLAY;
        for(ComponentsEnum conv : marginsDB.keySet()){
            int[] primaryMargin = operations.priceCheckItem(conv.getPrimaryItemID(), conv.getGeSearchTerm());
            int[] finishedMargin = operations.priceCheckItem(conv.getFinishedItemID(), conv.getGeSearchTerm());
            int[] convMargin = {primaryMargin[1], finishedMargin[0]};
            marginsDB.replace(conv, convMargin);
            script.log(conv.name() + " unf potion conversion has margin: " + Arrays.toString(convMargin));
            int marginDelta = convMargin[1] - convMargin[0];
            if(marginDelta > bestDeltaMargin){
                bestDeltaMargin = marginDelta;
                best = conv;
            }
            lastUpdateTimestamps.put(conv, Instant.now());
        }
        if(best != ComponentsEnum.CLAY){
            script.log(best.name() + " has best delta margin at: " + bestDeltaMargin + " with margin: " + Arrays.toString(marginsDB.get(best)));
        } else {
            script.log("price check all operation failed");
            return ComponentsEnum.IRIT;
        }
        return best;
    }

    public int getSecondsSinceLastUpdate(ComponentsEnum componentsEnum) {
        Instant lastUpdate = lastUpdateTimestamps.get(componentsEnum);
        if(lastUpdate != null){
            return (int) (Instant.now().getEpochSecond() - lastUpdate.getEpochSecond());
        }
        return Integer.MAX_VALUE;
    }

    public int[] getMargin(ComponentsEnum componentsEnum) {
        int[] margin = marginsDB.get(componentsEnum);
        if(margin != null){
            return margin;
        }
        return new int[2];
    }
}
