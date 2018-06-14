package Nodes.BankingNodes.VialWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw14Secondary extends AbstractWithdrawSecondary {

    public Withdraw14Secondary(Script script, ComponentsEnum secondaryItem) {
        super(script, secondaryItem);
    }

    @Override
    boolean withdrawSecondary() {
        if(!script.getInventory().isEmptyExcept(components.getPrimaryItemName()))
            script.getBank().depositAllExcept(components.getPrimaryItemName());
        return script.getBank().withdraw(components.getSecondaryItemName(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
