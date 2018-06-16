package Nodes.BankingNodes.VialWithdraw;

import org.osbot.rs07.script.Script;

public class Withdraw10Secondary extends AbstractWithdrawSecondary {

    public Withdraw10Secondary(Script script) {
        super(script);
    }

    @Override
    boolean withdrawSecondary() {
        if(!script.getInventory().isEmptyExcept(recipe.getPrimaryItemName()))
            script.getBank().depositAllExcept(recipe.getPrimaryItemName());
        return script.getBank().withdraw(recipe.getSecondaryItemName(), 10);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
