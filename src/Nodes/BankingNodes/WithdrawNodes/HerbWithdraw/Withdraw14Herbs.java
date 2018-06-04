package Nodes.BankingNodes.WithdrawNodes.HerbWithdraw;

import Util.HerbEnum;
import org.osbot.rs07.script.Script;

public class Withdraw14Herbs extends AbstractHerbWithdraw {

    public Withdraw14Herbs(Script script, HerbEnum targetHerb) {
        super(script, targetHerb);
    }

    @Override
    boolean withdrawHerbs() {
        return script.getBank().withdraw(targetHerb.getItemName(), 14);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
