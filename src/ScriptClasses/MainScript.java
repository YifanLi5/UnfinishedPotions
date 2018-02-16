package ScriptClasses;

import Nodes.BankUNFPotionsNode;
import Nodes.CreateUNFPotionsNode;
import Nodes.ExecutableNode;
import Nodes.GEBuyNode;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(author = "PayPalMeRSGP", name = "GE TEST6", info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script{
    static final String SCRIPT_NAME = "UNF Potions";
    static final int BUILD_NUM = 1;

    MarkovNodeExecutor executor;
    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        ExecutableNode create = CreateUNFPotionsNode.getInstance(this);
        ExecutableNode bank = BankUNFPotionsNode.getInstance(this);

        executor = new MarkovNodeExecutor(bank);
        executor.addEdgeToNode(bank, create, 1);
        executor.addEdgeToNode(create, bank, 1);
    }

    @Override
    public int onLoop() throws InterruptedException {
        ExecutableNode ge = GEBuyNode.getInstance(this, 2998, "toadflax");
        //return executor.executeNodeThenTraverse();
        return ge.executeNodeAction();
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        Point pos = getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line
    }
}
