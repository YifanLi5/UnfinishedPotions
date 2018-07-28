package Nodes.BankingNodes.SecondaryWithdraw;

import org.osbot.rs07.script.Script;

public class Withdraw10Secondary extends AbstractWithdrawSecondary {

    public Withdraw10Secondary(Script script) {
        super(script);
    }

    @Override
    boolean withdrawSecondary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        return script.getBank().withdraw(recipe.getSecondaryItemName(), 10);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
