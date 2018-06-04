package ScriptClasses;

import Nodes.GENodes.GEBuyNode;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "buy testing0";
    static final int BUILD_NUM = 6;

    GEBuyNode buy;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        buy = (GEBuyNode) GEBuyNode.getInstance(this);

    }

    @Override
    public int onLoop() throws InterruptedException {
        return buy.executeNode();
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
