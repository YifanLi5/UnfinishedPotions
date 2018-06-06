package ScriptClasses;

import Nodes.BankingNodes.WithdrawNodes.DecideRestockNode;
import Nodes.BankingNodes.WithdrawNodes.DepositNode;
import Nodes.BankingNodes.WithdrawNodes.HerbWithdraw.Withdraw10Herbs;
import Nodes.BankingNodes.WithdrawNodes.HerbWithdraw.Withdraw14Herbs;
import Nodes.BankingNodes.WithdrawNodes.VialWithdraw.Withdraw10Vials;
import Nodes.BankingNodes.WithdrawNodes.VialWithdraw.Withdraw14Vials;
import Nodes.CreationNodes.BasicCreation;
import Nodes.GENodes.GEBuyNode;
import Nodes.GENodes.GESellNode;
import Util.HerbAndPotionsEnum;
import Util.NoSuitableNodesException;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "test";
    static final int BUILD_NUM = 001;

    private HerbAndPotionsEnum targetItem = HerbAndPotionsEnum.TOADFLAX;
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
        buy = new GEBuyNode(this, targetItem);
        sell = new GESellNode(this, targetItem);
        BasicCreation create = new BasicCreation(this, targetItem);

        Withdraw10Herbs w10H_1 = new Withdraw10Herbs(this, targetItem);
        Withdraw10Herbs w10H_2 = new Withdraw10Herbs(this, targetItem);
        Withdraw14Herbs w14H_1 = new Withdraw14Herbs(this, targetItem);
        Withdraw14Herbs w14H_2 = new Withdraw14Herbs(this, targetItem);
        Withdraw10Vials w10V_1 = new Withdraw10Vials(this);
        Withdraw10Vials w10V_2 = new Withdraw10Vials(this);
        Withdraw14Vials w14V_1 = new Withdraw14Vials(this);
        Withdraw14Vials w14V_2 = new Withdraw14Vials(this);

        DepositNode deposit = new DepositNode(this);
        DecideRestockNode restock = new DecideRestockNode(this, targetItem);

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
        executor.addCondEdgeToNode(sell, buy, 1);
        executor.addCondEdgeToNode(buy, deposit, 1);
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
