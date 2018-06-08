package Nodes.BankingNodes.WithdrawNodes.HerbWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw14Primary extends AbstractPrimaryComponentWithdraw {

    public Withdraw14Primary(Script script, ComponentsEnum targetHerb) {
        super(script, targetHerb);
    }

    @Override
    boolean withdrawHerbs() {
        return script.getBank().withdraw(components.getPrimaryItemName(), 14);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
