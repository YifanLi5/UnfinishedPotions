package ScriptClasses;

import Nodes.BankingNodes.*;
import Nodes.CreationNodes.HoverBank;
import Nodes.CreationNodes.MouseOffscreen;
import Nodes.ExecutableNode;
import Nodes.GEBuyNode;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(author = "PayPalMeRSGP", name = "test1", info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script{
    static final String SCRIPT_NAME = "UNF Potions";
    static final int BUILD_NUM = 1;

    MarkovNodeExecutor executor;
    @Override
    public void onStart() throws InterruptedException {
        super.onStart();

        //creation based nodes
        ExecutableNode hover = HoverBank.getInstance(this);
        ExecutableNode offscreen = MouseOffscreen.getInstance(this);

        //banking based nodes
        ExecutableNode herbFirst = HerbFirstWithdraw.getInstance(this);
        ExecutableNode herbFirst3Step = HerbFirstThreeStepWithdraw.getInstance(this);
        ExecutableNode vialFirst = VialFirstWithdraw.getInstance(this);
        ExecutableNode vialFirst3Step = VialFirstThreeStepWithdraw.getInstance(this);

        executor = new MarkovNodeExecutor(herbFirst);

        executor.addEdgeToNode(herbFirst, hover, 50);
        executor.addEdgeToNode(herbFirst, offscreen, 50);

        executor.addEdgeToNode(herbFirst3Step, hover, 50);
        executor.addEdgeToNode(herbFirst3Step, offscreen, 50);

        executor.addEdgeToNode(vialFirst, hover, 50);
        executor.addEdgeToNode(vialFirst, offscreen, 50);

        executor.addEdgeToNode(vialFirst3Step, hover, 50);
        executor.addEdgeToNode(vialFirst3Step, offscreen, 50);

        executor.addEdgeToNode(hover, herbFirst, 40);
        executor.addEdgeToNode(hover, vialFirst, 40);
        executor.addEdgeToNode(hover, herbFirst3Step, 10);
        executor.addEdgeToNode(hover, vialFirst3Step, 10);

        executor.addEdgeToNode(offscreen, herbFirst, 40);
        executor.addEdgeToNode(offscreen, vialFirst, 40);
        executor.addEdgeToNode(offscreen, herbFirst3Step, 10);
        executor.addEdgeToNode(offscreen, vialFirst3Step, 10);

    }

    @Override
    public int onLoop() throws InterruptedException {
        /*ExecutableNode ge = GEBuyNode.getInstance(this, 2998, "toadflax");
        return ge.executeNodeAction();*/
        return executor.executeNodeThenTraverse();
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        Point pos = getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line
    }
}
