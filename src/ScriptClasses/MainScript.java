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
import Nodes.DebuggingNode;
import Nodes.GENodes.*;
import Nodes.MarkovChain.MarkovNodeExecutor;
import Nodes.StartingNode;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static ScriptClasses.MainScript.SCRIPT_NAME;

@ScriptManifest(author = "PayPalMeRSGP", name = SCRIPT_NAME, info = "item combiner, but mainly used for unf potions", version = 0.5, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "Item_Combinator";

    private MarkovNodeExecutor executor;

    private DebuggingNode debug;
    private boolean runDebugNode = false;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        Statics.script = this;
        markovChainSetup();
        camera.movePitch(67);
        new ScriptPaint(this);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            return executor.executeThenTraverse();
        } catch (NullPointerException ex){
            log(ex.getMessage());
            MethodProvider.sleep(1000);
            return 1000;
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

        AbortRelevantOffers abort = new AbortRelevantOffers(this);
        GESpinLockBuyNode buy = new GESpinLockBuyNode(this);
        GESpinLockSellNode sell = new GESpinLockSellNode(this);
        IntermittentBuy randBuy = new IntermittentBuy(this);
        IntermittentSell randSell = new IntermittentSell(this);
        InitialBuy initialBuy = new InitialBuy(this);

        executor = new MarkovNodeExecutor(start, w10P, w14P, wXP, w10S, w14S, wXS, restock, deposit, fix, afk, hover, premature, buy, sell, randBuy, randSell, initialBuy, abort);
    }

    @Override
    public void onExit() throws InterruptedException {
        super.onExit();
        Margins.markSingletonAsNull();
        ScriptPaint.geOpsEnabled = true;
    }
}
