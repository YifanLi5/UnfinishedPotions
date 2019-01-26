package Nodes.BankingNodes.Withdraw;
import Util.Margins;
import org.osbot.rs07.Bot;

public class WithdrawPrimary extends AbstractWithdraw {

    public WithdrawPrimary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawItem() {
        return bank.withdraw(recipe.getPrimary(), 14);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        recipe = Margins.getInstance(bot).getCurrentRecipe();
        return !inventory.contains(recipe.getPrimary());
    }

}
