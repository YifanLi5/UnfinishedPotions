package ScriptClasses;

import GrandExchange_Util.GrandExchangeEventDispatcher;
import GrandExchange_Util.GrandExchangeOperations;
import GrandExchange_Util.GrandExchangeOffer;
import Nodes.BankingNodes.SubOptimalWithdrawVariation0;
import Nodes.BankingNodes.OptimalWithdrawVariation0;
import Nodes.BankingNodes.SubOptimalWithdrawVariation1;
import Nodes.BankingNodes.OptimalWithdrawVariation1;
import Nodes.CreationNodes.HoverBank;
import Nodes.CreationNodes.MouseOffscreen;
import Nodes.ExecutableNode;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.Arrays;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script implements GrandExchangeEventDispatcher.GrandExchangeListener{
    static final String SCRIPT_NAME = "GE_testing";
    static final int BUILD_NUM = 6;

    GrandExchangeOffer buyOffer;
    GrandExchangeOperations operations = new GrandExchangeOperations(this);
    GrandExchangeEventDispatcher geEvents = new GrandExchangeEventDispatcher(this);

    MarkovNodeExecutor executor;
    @Override
    public void onStart() throws InterruptedException {
        super.onStart();

        //creation based nodes
        ExecutableNode hover = HoverBank.getInstance(this);
        ExecutableNode offscreen = MouseOffscreen.getInstance(this);

        //banking based nodes
        ExecutableNode herbFirst = OptimalWithdrawVariation0.getInstance(this);
        ExecutableNode herbFirst3Step = SubOptimalWithdrawVariation0.getInstance(this);
        ExecutableNode vialFirst = OptimalWithdrawVariation1.getInstance(this);
        ExecutableNode vialFirst3Step = SubOptimalWithdrawVariation1.getInstance(this);

        executor = new MarkovNodeExecutor(herbFirst);

        executor.addEdgeToNode(herbFirst, hover, 40);
        executor.addEdgeToNode(herbFirst, offscreen, 60);

        executor.addEdgeToNode(herbFirst3Step, hover, 40);
        executor.addEdgeToNode(herbFirst3Step, offscreen, 60);

        executor.addEdgeToNode(vialFirst, hover, 25);
        executor.addEdgeToNode(vialFirst, offscreen, 75);

        executor.addEdgeToNode(vialFirst3Step, hover, 25);
        executor.addEdgeToNode(vialFirst3Step, offscreen, 75);

        executor.addEdgeToNode(hover, herbFirst, 45);
        executor.addEdgeToNode(hover, vialFirst, 45);
        executor.addEdgeToNode(hover, herbFirst3Step, 5);
        executor.addEdgeToNode(hover, vialFirst3Step, 5);

        executor.addEdgeToNode(offscreen, herbFirst, 45);
        executor.addEdgeToNode(offscreen, vialFirst, 45);
        executor.addEdgeToNode(offscreen, herbFirst3Step, 5);
        executor.addEdgeToNode(offscreen, vialFirst3Step, 5);

    }

    @Override
    public int onLoop() throws InterruptedException {
        int[] margin = operations.priceCheckItem(2998, "toadflax", 5000);
        log(Arrays.toString(margin));
        //return executor.executeNodeThenTraverse();
        return 10000;
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        Point pos = getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line
    }

    @Override
    public void onExit() throws InterruptedException {
        super.onExit();
        geEvents.stopQueryingOffers();
    }

    @Override
    public void onGEUpdate(GrandExchangeOffer offer) {
        if(offer.equals(this.buyOffer)){
            GrandExchange.Status status = offer.getBoxStatus();
            log("Offer for box " + offer.getSelectedBox().toString() + " updated. " +
                    "\nIt is now " + status.toString());
        }
    }
}
