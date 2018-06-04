package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.HerbEnum;
import org.osbot.rs07.script.Script;

public class Withdraw14Vials extends AbstractVialWithdraw{

    public Withdraw14Vials(Script script) {
        super(script);
    }

    @Override
    boolean withdrawVials() {
        return script.getBank().withdraw(HerbEnum.VIAL_OF_WATER.getItemName(), 14);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
