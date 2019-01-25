package Nodes.BankingNodes.SecondaryWithdraw;

import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXSecondary extends AbstractWithdrawSecondary {
    public WithdrawXSecondary(Bot bot) {
        super(bot);
    }

    @Override
    boolean withdrawSecondary() throws InterruptedException {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        if(bank.interact("Withdraw-X", recipe.getSecondary())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen){
                Statics.shortRandomNormalDelay();
                return bank.withdraw(recipe.getSecondary(), 14);
            }

        }
        return false;
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    private boolean isNumberEntryOpen(){
        RS2Widget numberEntry = widgets.getWidgetContainingText(162, "Enter amount:");
        return numberEntry != null && numberEntry.isVisible();
    }
}
