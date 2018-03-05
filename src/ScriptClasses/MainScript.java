package ScriptClasses;

import GrandExchange_Util.GrandExchangeObserver;
import GrandExchange_Util.GrandExchangeOperations;
import GrandExchange_Util.GrandExchangeOffer;
import Nodes.BankingNodes.*;
import Nodes.CreationNodes.AbstractCreationNode;
import Nodes.ExecutableNode;
import Nodes.GENodes.GEBuyNode;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.ArrayList;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "GE_testing";
    static final int BUILD_NUM = 6;

    private static final HerbEnum cleanHerb = HerbEnum.TOADFLAX;

    GrandExchangeOffer buyOffer;
    GrandExchangeOperations operations = new GrandExchangeOperations(this);
    GrandExchangeObserver geEvents = new GrandExchangeObserver(this);

    MarkovNodeExecutor executor;
    @Override
    public void onStart() throws InterruptedException {
        super.onStart();

        //creation based nodes
        ArrayList<ExecutableNode> creationNodes = AbstractCreationNode.getInheritingNodes(this, cleanHerb);

        //banking based nodes
        ArrayList<ExecutableNode> bankingNodes = AbstractBankNode.getInheritingNodes(this, cleanHerb);
        executor = new MarkovNodeExecutor(bankingNodes.get(0));

        //add edges from all creation to bank nodes, and vice versa.
        for(ExecutableNode creationNode: creationNodes){
            for(ExecutableNode bankNode: bankingNodes){
                executor.addEdgeToNode(creationNode, bankNode, bankNode.getDefaultEdgeWeight());
                executor.addEdgeToNode(bankNode, creationNode, creationNode.getDefaultEdgeWeight());
            }
        }

        //after buying, go back to bank node
        ExecutableNode buy = GEBuyNode.getInstance(this, cleanHerb);
        executor.addEdgeToNode(buy, bankingNodes.get(0), 1);
    }

    @Override
    public int onLoop() throws InterruptedException {
        /*int[] margin = operations.priceCheckItem(2998, "toadflax", 5000);
        log(Arrays.toString(margin));*/
        return executor.executeNodeThenTraverse();
        //return 10000;
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

    public MarkovNodeExecutor getExecutor() {
        return executor;
    }
}
