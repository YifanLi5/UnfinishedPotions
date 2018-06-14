package Nodes.BankingNodes.HerbWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw14Primary extends AbstractWithdrawPrimary {

    public Withdraw14Primary(Script script, ComponentsEnum targetHerb) {
        super(script, targetHerb);
    }

    @Override
    boolean withdrawPrimary() {
        if(!script.getInventory().isEmptyExcept(components.getSecondaryItemName()))
            script.getBank().depositAllExcept(components.getSecondaryItemName());
        return script.getBank().withdraw(components.getPrimaryItemName(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
