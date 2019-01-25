package ScriptClasses;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.BankingNodes.OptionalInvFixNode;
import Nodes.BankingNodes.PrimaryWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.PrimaryWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.PrimaryWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.WithdrawXSecondary;
import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.GENodes.*;
import Nodes.MarkovChain.MarkovNodeExecutor;
import Nodes.StartingNode;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = SCRIPT_NAME, info = "item combiner, but mainly used for unf potions", version = 0.5, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "Item_Combinator";
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
        Withdraw10Primary w10P = new Withdraw10Primary(bot);
        Withdraw14Primary w14P = new Withdraw14Primary(bot);
        WithdrawXPrimary wXP = new WithdrawXPrimary(bot);

        Withdraw10Secondary w10S = new Withdraw10Secondary(bot);
        Withdraw14Secondary w14S = new Withdraw14Secondary(bot);
        WithdrawXSecondary wXS = new WithdrawXSecondary(bot);

        DecideRestockNode restock = new DecideRestockNode(bot);
        DepositNode deposit = new DepositNode(bot);
        OptionalInvFixNode fix = new OptionalInvFixNode(bot);

        AFKCreation afk = new AFKCreation(bot);
        HoverBankerCreation hover = new HoverBankerCreation(bot);
        PrematureStopCreation premature = new PrematureStopCreation(bot);

        AbortRelevantOffers abort = new AbortRelevantOffers(bot);
        WaitUntilBuy buy = new WaitUntilBuy(bot);
        WaitUntilSell sell = new WaitUntilSell(bot);
        IntermittentBuy randBuy = new IntermittentBuy(bot);
        IntermittentSell randSell = new IntermittentSell(bot);
        InitialBuyWaitUntil initialBuy = new InitialBuyWaitUntil(bot);

        executor = new MarkovNodeExecutor(start, w10P, w14P, wXP, w10S, w14S, wXS, restock, deposit, fix, afk, hover, premature, buy, sell, randBuy, randSell, initialBuy, abort);
    }

    @Override
    public void onExit() throws InterruptedException {
        super.onExit();
        Margins.markSingletonAsNull();
        ScriptPaint.geOpsEnabled = true;
    }
}
