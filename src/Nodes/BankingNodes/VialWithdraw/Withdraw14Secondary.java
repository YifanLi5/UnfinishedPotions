package Nodes.BankingNodes.VialWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import java.util.concurrent.ThreadLocalRandom;

public class Withdraw14Secondary extends AbstractWithdrawSecondary {

    public Withdraw14Secondary(Script script, ComponentsEnum secondaryItem) {
        super(script, secondaryItem);
    }

    @Override
    boolean withdrawSecondary() {
        if(script.getInventory().contains(components.getPrimaryItemName())){
            if(ThreadLocalRandom.current().nextBoolean()){
                return script.getBank().withdraw(components.getSecondaryItemName(), Bank.WITHDRAW_ALL);
            }
        }
        return script.getBank().withdraw(components.getSecondaryItemName(), 14);
    }

    @Override
    public boolean isJumping() {
        return false;
    }
}
