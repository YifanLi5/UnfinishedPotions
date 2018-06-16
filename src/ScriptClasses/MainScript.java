package ScriptClasses;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.BankingNodes.HerbWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.HerbWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.HerbWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.OptionalInvFixNode;
import Nodes.BankingNodes.VialWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.VialWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.VialWithdraw.WithdrawXSecondary;
import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.DebuggingNode;
import Nodes.GENodes.GESpinLockBuyNode;
import Nodes.GENodes.GESpinLockSellNode;
import Nodes.MarkovChain.MarkovNodeExecutor;
import Nodes.StartingNode;
import Util.Statics;
import Util.UnfPotionRecipes;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

import static ScriptClasses.MainScript.BUILD_NUM;
import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = BUILD_NUM + SCRIPT_NAME, info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "convMargins";
    static final int BUILD_NUM = 3;
    private UnfPotionRecipes debugComponent = UnfPotionRecipes.IRIT;

    private MarkovNodeExecutor executor;
    private GESpinLockBuyNode buy;
    private GESpinLockSellNode sell;

    private DebuggingNode debug;
    private boolean runDebugNode = false;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        Statics.script = this;
        if(runDebugNode){
            debug = new DebuggingNode(this);
        } else {
            markovChainSetup();
            camera.movePitch(random(60, 67));
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        if(runDebugNode){
            return debug.executeNode();
        }
        else {
            return executor.executeThenTraverse();
        }
    }

    private void markovChainSetup(){
        StartingNode start = new StartingNode(this);
        Withdraw10Primary w10P = new Withdraw10Primary(this);
        Withdraw14Primary w14P = new Withdraw14Primary(this);
        WithdrawXPrimary wXP = new WithdrawXPrimary(this);

        Withdraw10Secondary w10S = new Withdraw10Secondary(this);
        Withdraw14Secondary w14S = new Withdraw14Secondary(this);
        WithdrawXSecondary wXS = new WithdrawXSecondary(this);

        DecideRestockNode restock = new DecideRestockNode(this);
        DepositNode deposit = new DepositNode(this);
        OptionalInvFixNode fix = new OptionalInvFixNode(this);

        AFKCreation afk = new AFKCreation(this);
        HoverBankerCreation hover = new HoverBankerCreation(this);
        PrematureStopCreation premature = new PrematureStopCreation(this);

        buy = new GESpinLockBuyNode(this);
        sell = new GESpinLockSellNode(this);

        executor = new MarkovNodeExecutor(start, w10P, w14P, wXP, w10S, w14S, wXS, restock, deposit, fix, afk, hover, premature, buy, sell);
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
        if(runDebugNode){
            debug.stop();
        } else{
            buy.stopThread();
            sell.stopThread();
        }
    }


}
