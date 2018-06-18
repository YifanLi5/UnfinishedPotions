package Nodes.BankingNodes.VialWithdraw;

import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXSecondary extends AbstractWithdrawSecondary {
    public WithdrawXSecondary(Script script) {
        super(script);
    }

    @Override
    boolean withdrawSecondary() throws InterruptedException {
        if(containsForeignItem()){
            isJumping = true;
        }
        Bank bank = script.getBank();
        if(!script.getInventory().isEmptyExcept(recipe.getPrimaryItemName()))
            script.getBank().depositAllExcept(recipe.getPrimaryItemName());
        if(bank.interact("Withdraw-X", recipe.getSecondaryItemName())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen){
                Statics.shortRandomNormalDelay();
                return bank.withdraw(recipe.getSecondaryItemName(), 14);
            }

        }
        return false;
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    private boolean isNumberEntryOpen(){
        RS2Widget numberEntry = script.getWidgets().getWidgetContainingText(162, "Enter amount:");
        return numberEntry != null && numberEntry.isVisible();
    }
}
