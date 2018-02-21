package ScriptClasses;

import Nodes.BankingNodes.HerbFirstThreeStepWithdraw;
import Nodes.BankingNodes.HerbFirstWithdraw;
import Nodes.BankingNodes.VialFirstThreeStepWithdraw;
import Nodes.BankingNodes.VialFirstWithdraw;
import Nodes.CreationNodes.HoverBank;
import Nodes.CreationNodes.MouseOffscreen;
import Nodes.ExecutableNode;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script{
    static final String SCRIPT_NAME = "UNF Potions";
    static final int BUILD_NUM = 4;

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

        executor.addEdgeToNode(herbFirst, hover, 30);
        executor.addEdgeToNode(herbFirst, offscreen, 70);

        executor.addEdgeToNode(herbFirst3Step, hover, 30);
        executor.addEdgeToNode(herbFirst3Step, offscreen, 70);

        executor.addEdgeToNode(vialFirst, hover, 30);
        executor.addEdgeToNode(vialFirst, offscreen, 70);

        executor.addEdgeToNode(vialFirst3Step, hover, 30);
        executor.addEdgeToNode(vialFirst3Step, offscreen, 70);

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
