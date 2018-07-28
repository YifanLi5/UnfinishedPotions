package Nodes.BankingNodes.PrimaryWithdraw;

import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXPrimary extends AbstractWithdrawPrimary {
    public WithdrawXPrimary(Script script) {
        super(script);
    }

    @Override
    boolean withdrawPrimary() throws InterruptedException {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        Bank bank = script.getBank();
        if(!script.getInventory().isEmptyExcept(recipe.getSecondaryItemName()))
            script.getBank().depositAllExcept(recipe.getSecondaryItemName());
        if(bank.interact("Withdraw-X", recipe.getPrimaryItemName())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen){
                Statics.shortRandomNormalDelay();
                return bank.withdraw(recipe.getPrimaryItemName(), 14);
            }
        }
        return false;
    }

    private boolean isNumberEntryOpen(){
        RS2Widget numberEntry = script.getWidgets().getWidgetContainingText(162, "Enter amount:");
        return numberEntry != null && numberEntry.isVisible();
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
