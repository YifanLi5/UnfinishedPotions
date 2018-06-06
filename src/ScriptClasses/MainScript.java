package ScriptClasses;

import Nodes.GENodes.GEBuyNode;
import Nodes.GENodes.GESellNode;
import Util.NoSuitableNodesException;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "buy/sell";
    static final int BUILD_NUM = 9;

    GEBuyNode buy;
    GESellNode sell;
    MarkovNodeExecutor executor;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        buy = new GEBuyNode(this);
        sell = new GESellNode(this);
        executor = new MarkovNodeExecutor(buy);

        executor.addNormalEdgeToNode(buy, sell, 1);
        executor.addNormalEdgeToNode(sell, buy, 1);

    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            return executor.executeThenTraverse();
        } catch (NoSuitableNodesException e) {
            stop(false);
            e.printStackTrace();
        }
        return 0;
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
        buy.stopThread();

    }


}
