package Nodes.BankingNodes.PrimaryWithdraw;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXPrimary extends AbstractWithdrawPrimary {
    public WithdrawXPrimary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawPrimary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        if(bank.interact("Withdraw-X", recipe.getPrimary())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen){
                return bank.withdraw(recipe.getPrimary(), 14);
            }
        }
        return false;
    }

    private boolean isNumberEntryOpen(){
        RS2Widget numberEntry = widgets.getWidgetContainingText(162, "Enter amount:");
        return numberEntry != null && numberEntry.isVisible();
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
