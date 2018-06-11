package Nodes.BankingNodes.WithdrawNodes.HerbWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class WithdrawXPrimary extends AbstractWithdrawPrimary {
    public WithdrawXPrimary(Script script, ComponentsEnum component) {
        super(script, component);
    }

    @Override
    boolean withdrawPrimary() {
        Bank bank = script.getBank();
        if(bank.interact("Withdraw-X", components.getPrimaryItemName())){
            boolean isOpen = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return isNumberEntryOpen();
                }
            }.sleep();
            if(isOpen){
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
    public boolean doConditionalTraverse() {
        return false;
    }
}
