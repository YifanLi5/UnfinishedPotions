package Nodes.BankingNodes.SecondaryWithdraw;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;

import java.util.concurrent.ThreadLocalRandom;

public class Withdraw14Secondary extends AbstractWithdrawSecondary {

    public Withdraw14Secondary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawSecondary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        if(inventory.contains(recipe.getPrimary())){
            if(ThreadLocalRandom.current().nextBoolean()){
                return bank.withdraw(recipe.getSecondary(), Bank.WITHDRAW_ALL);
            }
        }
        return bank.withdraw(recipe.getSecondary(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
