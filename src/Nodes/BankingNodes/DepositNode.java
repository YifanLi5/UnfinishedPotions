package Nodes.BankingNodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.Statics;
import org.osbot.rs07.api.Menu;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;


public class DepositNode implements ExecutableNode {
    private Script script;
    private List<Edge> adjNodes = Collections.singletonList(new Edge(DecideRestockNode.class, 1));

    public DepositNode(Script script){
        this.script = script;

    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return script.getNpcs().closestThatContains("Banker").exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        boolean open = new ConditionalSleep(5000) {
            @Override
            public boolean condition() throws InterruptedException {
                return script.getBank().isOpen() || rightClickOpenBank();
            }
        }.sleep();
        if(open){
            if(!script.getInventory().isEmpty()){
                boolean deposited = new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() throws InterruptedException {
                        return script.getBank().depositAll();
                    }
                }.sleep();
                if(deposited){
                    return (int) Statics.randomNormalDist(500, 250);
                }
            }

        }
        return 0;
    }

    private boolean rightClickOpenBank() throws InterruptedException {
        script.getWidgets().closeOpenInterface();
        if(hoverOverBankOption()){
            Statics.shortRandomNormalDelay();
            return script.getMouse().click(false);
        }
        return false;
    }

    private boolean hoverOverBankOption() throws InterruptedException {
        NPC banker = script.getNpcs().closest("Banker");
        Mouse mouse = script.getMouse();
        Menu menu = script.getMenuAPI();
        boolean success = false;
        if(banker != null){
            boolean found = false;
            int idx = 0;
            int attempts = 0;
            while(!found && attempts++ < 5){
                if(mouse.click(new EntityDestination(script.getBot(), banker), true)){
                    if(menu.isOpen()){
                        List<Option> options = menu.getMenu();
                        for(; idx < options.size(); idx++){
                            if(options.get(idx).action.equals("Bank")){
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            if(found){
                Statics.shortRandomNormalDelay();
                RectangleDestination bankOptionRect = new RectangleDestination(script.getBot(), menu.getOptionRectangle(idx));
                success = mouse.move(bankOptionRect);
            }

        }
        return success;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
