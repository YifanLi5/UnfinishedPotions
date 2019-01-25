package Nodes.BankingNodes.SecondaryWithdraw;

import org.osbot.rs07.Bot;

public class Withdraw10Secondary extends AbstractWithdrawSecondary {

    public Withdraw10Secondary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawSecondary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        return bank.withdraw(recipe.getSecondary(), 10);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
