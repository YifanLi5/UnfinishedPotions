package Nodes.BankingNodes.PrimaryWithdraw;

import org.osbot.rs07.script.Script;

public class Withdraw10Primary extends AbstractWithdrawPrimary {



    public Withdraw10Primary(Script script) {
        super(script);
    }

    @Override
    boolean withdrawPrimary() {
        if(containsForeignItem()){
            isJumping = true;
            return false;
        }
        return script.getBank().withdraw(recipe.getPrimaryItemName(), 10);
    }



}
