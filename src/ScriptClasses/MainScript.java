package ScriptClasses;

import Nodes.BankingNodes.WithdrawNodes.DecideRestockNode;
import Nodes.BankingNodes.WithdrawNodes.DepositNode;
import Nodes.BankingNodes.WithdrawNodes.HerbWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.WithdrawNodes.HerbWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.WithdrawNodes.VialWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.WithdrawNodes.VialWithdraw.Withdraw14Secondary;
import Nodes.CreationNodes.BasicCreation;
import Nodes.GENodes.GEBuyNode;
import Nodes.GENodes.GESellNode;
import Util.ComponentsEnum;
import Util.NoSuitableNodesException;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "test";
    static final int BUILD_NUM = 3;

    private ComponentsEnum debugComponent = ComponentsEnum.CLAY;

    MarkovNodeExecutor executor;
    GEBuyNode buy;
    GESellNode sell;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        markovChainSetup();
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

    private void markovChainSetup(){
        buy = new GEBuyNode(this, debugComponent);
        sell = new GESellNode(this, debugComponent);
        BasicCreation create = new BasicCreation(this, debugComponent);

        Withdraw10Primary w10H_1 = new Withdraw10Primary(this, debugComponent);
        Withdraw10Primary w10H_2 = new Withdraw10Primary(this, debugComponent);
        Withdraw14Primary w14H_1 = new Withdraw14Primary(this, debugComponent);
        Withdraw14Primary w14H_2 = new Withdraw14Primary(this, debugComponent);
        Withdraw10Secondary w10V_1 = new Withdraw10Secondary(this, debugComponent);
        Withdraw10Secondary w10V_2 = new Withdraw10Secondary(this, debugComponent);
        Withdraw14Secondary w14V_1 = new Withdraw14Secondary(this, debugComponent);
        Withdraw14Secondary w14V_2 = new Withdraw14Secondary(this, debugComponent);

        DepositNode deposit = new DepositNode(this);
        DecideRestockNode restock = new DecideRestockNode(this, debugComponent);

        executor = new MarkovNodeExecutor(deposit);
        executor.addNormalEdgeToNode(deposit, restock, 1);

        executor.addNormalEdgeToNode(restock, w14V_1, 50);
        executor.addNormalEdgeToNode(restock, w10V_1, 2);
        executor.addNormalEdgeToNode(restock, w14H_1, 50);
        executor.addNormalEdgeToNode(restock, w10H_1, 2);

        executor.addNormalEdgeToNode(w14V_1, w14H_2, 100);
        executor.addNormalEdgeToNode(w14V_1, w10H_2, 5);

        executor.addNormalEdgeToNode(w14H_1, w14V_2, 100);
        executor.addNormalEdgeToNode(w14H_1, w10V_2, 5);

        executor.addNormalEdgeToNode(w10V_1, w14H_2, 100);
        executor.addNormalEdgeToNode(w10V_1, w10H_2, 5);

        executor.addNormalEdgeToNode(w10H_1, w14V_2, 100);
        executor.addNormalEdgeToNode(w10H_1, w10V_2, 5);

        executor.addNormalEdgeToNode(w14H_2, create, 1);
        executor.addNormalEdgeToNode(w10H_2, create, 1);
        executor.addNormalEdgeToNode(w14V_2, create, 1);
        executor.addNormalEdgeToNode(w10V_2, create, 1);

        executor.addNormalEdgeToNode(create, deposit, 1);

        executor.addCondEdgeToNode(restock, sell, 1);
        executor.addNormalEdgeToNode(sell, buy, 1);
        executor.addNormalEdgeToNode(buy, deposit, 1);
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
