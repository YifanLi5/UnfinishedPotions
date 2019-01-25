package Nodes.BankingNodes.PrimaryWithdraw;

import org.osbot.rs07.Bot;

public class Withdraw10Primary extends AbstractWithdrawPrimary {

    public Withdraw10Primary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawPrimary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        return bank.withdraw(recipe.getPrimary(), 10);
    }



}
