package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXSecondary extends AbstractWithdrawSecondary {
    public WithdrawXSecondary(Script script, ComponentsEnum components) {
        super(script, components);
    }

    @Override
    boolean withdrawSecondary() throws InterruptedException {
        Bank bank = script.getBank();
        if(!script.getInventory().isEmptyExcept(components.getPrimaryItemName()))
            script.getBank().depositAllExcept(components.getPrimaryItemName());
        if(bank.interact("Withdraw-X", components.getSecondaryItemName())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen){
                Statics.shortRandomNormalDelay();
                return bank.withdraw(components.getSecondaryItemName(), 14);
            }

        }
        return false;
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }

    private boolean isNumberEntryOpen(){
        RS2Widget numberEntry = script.getWidgets().getWidgetContainingText(162, "Enter amount:");
        return numberEntry != null && numberEntry.isVisible();
    }
}
