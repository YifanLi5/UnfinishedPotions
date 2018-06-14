package Nodes.BankingNodes.HerbWithdraw;

import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXPrimary extends AbstractWithdrawPrimary {
    public WithdrawXPrimary(Script script, ComponentsEnum component) {
        super(script, component);
    }

    @Override
    boolean withdrawPrimary() throws InterruptedException {
        Bank bank = script.getBank();
        if(!script.getInventory().isEmptyExcept(components.getSecondaryItemName()))
            script.getBank().depositAllExcept(components.getSecondaryItemName());
        if(bank.interact("Withdraw-X", components.getPrimaryItemName())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen){
                Statics.longRandomNormalDelay();
                return bank.withdraw(components.getPrimaryItemName(), 14);
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
