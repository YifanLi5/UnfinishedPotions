package Nodes.BankingNodes.Withdraw;

import Util.Margins;
import org.osbot.rs07.Bot;

public class WithdrawSecondary extends AbstractWithdraw {

    public WithdrawSecondary(Bot bot) {
        super(bot);
    }

    @Override
    void withdrawItem() {
        bank.withdraw(recipe.getSecondary(), 14);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        recipe = Margins.getInstance(bot).getCurrentRecipe();
        return !inventory.contains(recipe.getSecondary());
    }
}
