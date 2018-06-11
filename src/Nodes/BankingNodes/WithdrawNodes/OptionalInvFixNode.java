package Nodes.BankingNodes.WithdrawNodes;

import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;

public class OptionalInvFixNode implements MarkovNodeExecutor.ExecutableNode{
    private ComponentsEnum components;
    private Script script;

    public OptionalInvFixNode(ComponentsEnum components, Script script) {
        this.components = components;
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        Inventory inv = script.getInventory();
        return inv.getAmount(components.getPrimaryItemName()) != 14
                || inv.getAmount(components.getSecondaryItemName()) != 14;
    }

    @Override
    public int executeNode() throws InterruptedException {
        Inventory inv = script.getInventory();
        Bank bank = script.getBank();
        if(bank.isOpen()){
            if(inv.getAmount(components.getPrimaryItemName()) != 14
                    && inv.getAmount(components.getSecondaryItemName()) != 14){
                if(bank.depositAll()){
                    if(script.getBank().withdraw(components.getPrimaryItemName(), 14)){
                        Statics.shortRandomNormalDelay();
                        script.getBank().withdraw(components.getSecondaryItemName(), 14);
                    }
                }
            } else if(inv.getAmount(components.getPrimaryItemName()) != 14){
                if(script.getBank().withdraw(components.getPrimaryItemName(), 14)){

                }
            } else {
                script.getBank().withdraw(components.getSecondaryItemName(), 14);
            }
        }
        return (int) Statics.randomNormalDist(600, 200);
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }

    @Override
    public void logNode() {

    }
}
