package Nodes.BankingNodes.PrimaryWithdraw;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import java.util.concurrent.ThreadLocalRandom;

public class Withdraw14Primary extends AbstractWithdrawPrimary {

    public Withdraw14Primary(Script script) {
        super(script);
    }

    @Override
    boolean withdrawPrimary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        if(script.getInventory().contains(recipe.getSecondaryItemName())){
            if(ThreadLocalRandom.current().nextBoolean()){
                return script.getBank().withdraw(recipe.getPrimaryItemName(), Bank.WITHDRAW_ALL);
            }
        }
        return script.getBank().withdraw(recipe.getPrimaryItemName(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
