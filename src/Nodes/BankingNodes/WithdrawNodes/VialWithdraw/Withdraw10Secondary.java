package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw10Secondary extends AbstractWithdrawSecondary {

    public Withdraw10Secondary(Script script, ComponentsEnum secondaryItem) {
        super(script, secondaryItem);
    }

    @Override
    boolean withdrawSecondary() {
        if(!script.getInventory().isEmptyExcept(components.getPrimaryItemName()))
            script.getBank().depositAllExcept(components.getPrimaryItemName());
        return script.getBank().withdraw(components.getSecondaryItemName(), 10);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
