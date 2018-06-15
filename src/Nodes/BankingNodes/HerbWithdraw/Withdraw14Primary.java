package Nodes.BankingNodes.HerbWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import java.util.concurrent.ThreadLocalRandom;

public class Withdraw14Primary extends AbstractWithdrawPrimary {

    public Withdraw14Primary(Script script, ComponentsEnum targetHerb) {
        super(script, targetHerb);
    }

    @Override
    boolean withdrawPrimary() {
        if(script.getInventory().contains(components.getSecondaryItemName())){
            if(ThreadLocalRandom.current().nextBoolean()){
                return script.getBank().withdraw(components.getPrimaryItemName(), Bank.WITHDRAW_ALL);
            }
        }
        return script.getBank().withdraw(components.getPrimaryItemName(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
