package Nodes.BankingNodes.Withdraw;

import org.osbot.rs07.Bot;

public class WithdrawSecondary extends AbstractWithdraw {

    WithdrawSecondary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawItem() {
        return bank.withdraw(recipe.getSecondary(), 14);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !inventory.contains(recipe.getSecondary());
    }
}
