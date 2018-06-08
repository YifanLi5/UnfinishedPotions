package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw10Secondary extends AbstractSecondaryWithdraw {

    public Withdraw10Secondary(Script script, ComponentsEnum secondaryItem) {
        super(script, secondaryItem);
    }

    @Override
    boolean withdrawVials() {
        return script.getBank().withdraw(components.getSecondaryItemName(), 10);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
