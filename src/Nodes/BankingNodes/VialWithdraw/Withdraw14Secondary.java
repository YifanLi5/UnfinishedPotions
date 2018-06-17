package Nodes.BankingNodes.VialWithdraw;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import java.util.concurrent.ThreadLocalRandom;

public class Withdraw14Secondary extends AbstractWithdrawSecondary {

    public Withdraw14Secondary(Script script) {
        super(script);
    }

    @Override
    boolean withdrawSecondary() {
        if(script.getInventory().contains(recipe.getPrimaryItemName())){
            if(ThreadLocalRandom.current().nextBoolean()){
                return script.getBank().withdraw(recipe.getSecondaryItemName(), Bank.WITHDRAW_ALL);
            }
        }
        return script.getBank().withdraw(recipe.getSecondaryItemName(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
