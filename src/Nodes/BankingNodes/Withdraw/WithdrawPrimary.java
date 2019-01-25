package Nodes.BankingNodes.Withdraw;
import org.osbot.rs07.Bot;

public class WithdrawPrimary extends AbstractWithdraw {

    WithdrawPrimary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawItem() {
        return bank.withdraw(recipe.getPrimary(), 14);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !inventory.contains(recipe.getPrimary());
    }

}
