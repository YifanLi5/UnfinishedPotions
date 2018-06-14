package Nodes.BankingNodes.HerbWithdraw;

import Util.ComponentsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw10Primary extends AbstractWithdrawPrimary {



    public Withdraw10Primary(Script script, ComponentsEnum targetHerb) {
        super(script, targetHerb);
    }

    @Override
    boolean withdrawPrimary() {
        return script.getBank().withdraw(components.getPrimaryItemName(), 10);
    }



}