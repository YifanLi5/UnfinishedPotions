package ScriptClasses;

import Nodes.BankingNodes.WithdrawNodes.DecideRestockNode;
import Nodes.BankingNodes.WithdrawNodes.DepositNode;
import Nodes.BankingNodes.WithdrawNodes.HerbWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.WithdrawNodes.HerbWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.WithdrawNodes.HerbWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.WithdrawNodes.OptionalInvFixNode;
import Nodes.BankingNodes.WithdrawNodes.VialWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.WithdrawNodes.VialWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.WithdrawNodes.VialWithdraw.WithdrawXSecondary;
import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.DebuggingNode;
import Nodes.GENodes.GEBuyNode;
import Nodes.GENodes.GESellNode;
import Util.ComponentsEnum;
import Util.NoSuitableNodesException;
import Util.Statics;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "unf";
    static final int BUILD_NUM = 10;

    private ComponentsEnum debugComponent = ComponentsEnum.TOADFLAX;

    MarkovNodeExecutor executor;
    GEBuyNode buy;
    GESellNode sell;

    DebuggingNode debug;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        Statics.debug = this;
        markovChainSetup();
        //debug = new DebuggingNode(this);
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
        //return debug.executeNode();
    }

    private void markovChainSetup(){
        buy = new GEBuyNode(this, debugComponent);
        sell = new GESellNode(this, debugComponent);

        OptionalInvFixNode fix = new OptionalInvFixNode(this, debugComponent);
        AFKCreation afk = new AFKCreation(this, debugComponent);
        HoverBankerCreation hover = new HoverBankerCreation(this, debugComponent);
        List<MarkovNodeExecutor.ExecutableNode> postWithdrawNodes = new ArrayList<>(Arrays.asList(fix, afk, hover));
        List<Integer> postWithdrawNodesExeWeights = new ArrayList<>(Arrays.asList(50, 30, 70));

        DepositNode deposit = new DepositNode(this);
        DecideRestockNode restock = new DecideRestockNode(this, debugComponent);

        Withdraw10Primary w10P_1 = new Withdraw10Primary(this, debugComponent);
        Withdraw14Primary w14P_1 = new Withdraw14Primary(this, debugComponent);
        WithdrawXPrimary wXP_1 = new WithdrawXPrimary(this, debugComponent);
        List<MarkovNodeExecutor.ExecutableNode> withdrawPrimary_1 = new ArrayList<>(Arrays.asList(w10P_1, w14P_1, wXP_1));
        List<Integer> withdrawPrimary_1_ExeWeights = new ArrayList<>(Arrays.asList(1, 50, 1));

        Withdraw10Secondary w10S_1 = new Withdraw10Secondary(this, debugComponent);
        Withdraw14Secondary w14S_1 = new Withdraw14Secondary(this, debugComponent);
        WithdrawXSecondary wXS_1 = new WithdrawXSecondary(this, debugComponent);
        List<MarkovNodeExecutor.ExecutableNode> withdrawSecondary_1 = new ArrayList<>(Arrays.asList(w10S_1, w14S_1, wXS_1));
        List<Integer> withdrawSecondary_1_ExeWeights = new ArrayList<>(Arrays.asList(1, 50, 1));

        Withdraw10Primary w10P_2 = new Withdraw10Primary(this, debugComponent);
        Withdraw14Primary w14P_2 = new Withdraw14Primary(this, debugComponent);
        WithdrawXPrimary wXP_2 = new WithdrawXPrimary(this, debugComponent);
        List<MarkovNodeExecutor.ExecutableNode> withdrawPrimary_2 = new ArrayList<>(Arrays.asList(w10P_2, w14P_2, wXP_2));
        List<Integer> withdrawPrimary_2_ExeWeights = new ArrayList<>(Arrays.asList(1, 100, 1));

        Withdraw10Secondary w10S_2 = new Withdraw10Secondary(this, debugComponent);
        Withdraw14Secondary w14S_2 = new Withdraw14Secondary(this, debugComponent);
        WithdrawXSecondary wXS_2 = new WithdrawXSecondary(this, debugComponent);
        List<MarkovNodeExecutor.ExecutableNode> withdrawSecondary_2 = new ArrayList<>(Arrays.asList(w10S_2, w14S_2, wXS_2));
        List<Integer> withdrawSecondary_2_ExeWeights = new ArrayList<>(Arrays.asList(1, 100, 1));

        executor = new MarkovNodeExecutor(deposit);
        executor.addNormalEdgeToNode(deposit, restock, 1);
        executor.addCondEdgeToNode(restock, sell, 1);
        executor.addNormalEdgeToNode(sell, buy, 1);
        executor.addNormalEdgeToNode(buy, deposit, 1);

        executor.addNormalEdgesToNode(restock, withdrawPrimary_1, withdrawPrimary_1_ExeWeights);
        executor.addNormalEdgesToNode(restock, withdrawSecondary_1, withdrawSecondary_1_ExeWeights);

        executor.addNormalEdgesToNode(w10P_1, withdrawSecondary_2, withdrawSecondary_2_ExeWeights);
        executor.addNormalEdgesToNode(w14P_1, withdrawSecondary_2, withdrawSecondary_2_ExeWeights);
        executor.addNormalEdgesToNode(wXP_1, withdrawSecondary_2, withdrawSecondary_2_ExeWeights);

        executor.addNormalEdgesToNode(w10S_1, withdrawPrimary_2, withdrawPrimary_2_ExeWeights);
        executor.addNormalEdgesToNode(w14S_1, withdrawPrimary_2, withdrawPrimary_2_ExeWeights);
        executor.addNormalEdgesToNode(wXS_1, withdrawPrimary_2, withdrawPrimary_2_ExeWeights);

        executor.addNormalEdgesToNode(w10P_2, postWithdrawNodes, postWithdrawNodesExeWeights);
        executor.addNormalEdgesToNode(w14P_2, postWithdrawNodes, postWithdrawNodesExeWeights);
        executor.addNormalEdgesToNode(wXP_2, postWithdrawNodes, postWithdrawNodesExeWeights);
        executor.addNormalEdgesToNode(w10S_2, postWithdrawNodes, postWithdrawNodesExeWeights);
        executor.addNormalEdgesToNode(w14S_2, postWithdrawNodes, postWithdrawNodesExeWeights);
        executor.addNormalEdgesToNode(wXS_2, postWithdrawNodes, postWithdrawNodesExeWeights);

        executor.addNormalEdgeToNode(fix, afk, 20);
        executor.addNormalEdgeToNode(fix, hover, 80);

        executor.addNormalEdgeToNode(afk, deposit, 1);
        executor.addNormalEdgeToNode(hover, deposit, 1);
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
        //debug.stop();

    }


}
