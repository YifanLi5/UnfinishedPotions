package Nodes.BankingNodes.WithdrawNodes.HerbWithdraw;

import Util.HerbEnum;
import org.osbot.rs07.script.Script;

public class Withdraw10Herbs extends AbstractHerbWithdraw {

    public Withdraw10Herbs(Script script, HerbEnum targetHerb) {
        super(script, targetHerb);
    }

    @Override
    boolean withdrawHerbs() {
        return script.getBank().withdraw(targetHerb.getItemName(), 10);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
