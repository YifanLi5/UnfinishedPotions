package ScriptClasses;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.BankingNodes.OptionalInvFixNode;
import Nodes.BankingNodes.Withdraw.WithdrawPrimary;
import Nodes.BankingNodes.Withdraw.WithdrawSecondary;
import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.GENodes.AbortRelevantOffers;
import Nodes.GENodes.Buy;
import Nodes.GENodes.InitialBuy;
import Nodes.GENodes.Sell;
import Nodes.MarkovChain.MarkovNodeExecutor;
import Nodes.StartingNode;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = SCRIPT_NAME, info = "item combiner, but mainly used for unf potions", version = 0.5, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "Unfinished_Potions v1.1.6";
    private MarkovNodeExecutor executor;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        Statics.script = this; //for debugging purposes, easy access to script.log method
        markovChainSetup();
        camera.movePitch(67); //move camera as far up as possible, less players in menu when right clicking
        new ScriptPaint(this);
    }

    @Override
    public int onLoop() throws InterruptedException {
        int sleepTime = executor.executeThenTraverse();
        if(sleepTime < 0){ //if any node returns -1, its time to stop script
            stop(false);
            return 5000;
        } else return sleepTime;
    }

    private void markovChainSetup(){
        StartingNode start = new StartingNode(bot);

        WithdrawPrimary wp = new WithdrawPrimary(bot);
        WithdrawSecondary ws = new WithdrawSecondary(bot);

        DecideRestockNode restock = new DecideRestockNode(bot);
        DepositNode deposit = new DepositNode(bot);
        OptionalInvFixNode fix = new OptionalInvFixNode(bot);

        AFKCreation afk = new AFKCreation(bot);
        HoverBankerCreation hover = new HoverBankerCreation(bot);
        PrematureStopCreation premature = new PrematureStopCreation(bot);

        AbortRelevantOffers abort = new AbortRelevantOffers(bot);
        Buy buy = new Buy(bot);
        Sell sell = new Sell(bot);
        InitialBuy initialBuy = new InitialBuy(bot);

        executor = new MarkovNodeExecutor(start, wp, ws, restock, deposit, fix, afk, hover, premature, buy, sell, initialBuy, abort);
    }

    @Override
    public void onExit() throws InterruptedException {
        super.onExit();
        Margins.markSingletonAsNull();
        ScriptPaint.geOpsEnabled = true;
    }
}
