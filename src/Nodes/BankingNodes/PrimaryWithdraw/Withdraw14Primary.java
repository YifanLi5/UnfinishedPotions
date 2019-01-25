package Nodes.BankingNodes.PrimaryWithdraw;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;

import java.util.concurrent.ThreadLocalRandom;

public class Withdraw14Primary extends AbstractWithdrawPrimary {

    public Withdraw14Primary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawPrimary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        if(inventory.contains(recipe.getSecondary()) && ThreadLocalRandom.current().nextBoolean()){
            return bank.withdraw(recipe.getPrimary(), Bank.WITHDRAW_ALL);
        }
        return bank.withdraw(recipe.getPrimary(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
