package Nodes.BankingNodes.WithdrawNodes.VialWithdraw;

import Util.HerbAndPotionsEnum;
import org.osbot.rs07.script.Script;

public class Withdraw10Vials extends AbstractVialWithdraw{

    public Withdraw10Vials(Script script) {
        super(script);
    }

    @Override
    boolean withdrawVials() {
        return script.getBank().withdraw(HerbAndPotionsEnum.VIAL_OF_WATER.getItemName(), 10);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}
