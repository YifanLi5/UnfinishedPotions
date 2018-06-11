package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXSecondary extends AbstractSecondaryWithdraw {
    public WithdrawXSecondary(Script script, ComponentsEnum components) {
        super(script, components);
    }

    @Override
    boolean withdrawSecondary() {
        Bank bank = script.getBank();
        if(bank.interact("Withdraw-X", components.getSecondaryItemName())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen)
                return bank.withdraw(components.getSecondaryItemName(), 14);
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
