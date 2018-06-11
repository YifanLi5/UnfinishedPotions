package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw14Secondary extends AbstractSecondaryWithdraw {

    public Withdraw14Secondary(Script script, ComponentsEnum secondaryItem) {
        super(script, secondaryItem);
    }

    @Override
    boolean withdrawSecondary() {
        return script.getBank().withdraw(components.getSecondaryItemName(), 14);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
