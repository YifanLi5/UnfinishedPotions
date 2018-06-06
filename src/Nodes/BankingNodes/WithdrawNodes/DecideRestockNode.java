package Nodes.BankingNodes.WithdrawNodes;

import ScriptClasses.MarkovNodeExecutor;
import Util.HerbAndPotionsEnum;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

public class DecideRestockNode implements MarkovNodeExecutor.ExecutableNode {
    private Script script;
    private HerbAndPotionsEnum item = HerbAndPotionsEnum.TOADFLAX;
    private boolean conditionalTraverse = false;

    public DecideRestockNode(Script script, HerbAndPotionsEnum item) {
        this.script = script;
        this.item = item;
    }

    @Override
    public boolean canExecute() {
        return script.getBank().isOpen();
    }

    @Override
    public int executeNode() throws InterruptedException {
        Bank bank = script.getBank();
        int herbsRemaining = (int) bank.getAmount(item.getItemName());
        if(herbsRemaining < 14)
            conditionalTraverse = true;

        return 0;
    }

    @Override
    public boolean doConditionalTraverse() {
        if(conditionalTraverse){
            conditionalTraverse = false;
            return true;
        }
        return false;
    }
}
