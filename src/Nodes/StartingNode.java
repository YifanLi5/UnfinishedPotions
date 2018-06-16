package Nodes;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ComponentsEnum;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;

import java.util.List;

public class StartingNode implements ExecutableNode {
    private Script script;
    Class<? extends ExecutableNode> jumpTarget;

    public StartingNode(Script script) {
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return true;
    }

    @Override
    public int executeNode() throws InterruptedException {
        Inventory inv = script.getInventory();
        if(invContainsPrimaryComponent() && inv.contains("Vial of water")){
            jumpTarget = HoverBankerCreation.class;
        } else if(inv.isEmpty()){
            jumpTarget = DecideRestockNode.class;
        } else {
            jumpTarget = DepositNode.class;
        }
        return 0;
    }

    private boolean invContainsPrimaryComponent(){
        Inventory inv = script.getInventory();
        return inv.contains(ComponentsEnum.AVANTOE.getPrimaryItemName())
                || inv.contains(ComponentsEnum.TOADFLAX.getPrimaryItemName())
                || inv.contains(ComponentsEnum.RANARR.getPrimaryItemName())
                || inv.contains(ComponentsEnum.IRIT.getPrimaryItemName())
                || inv.contains(ComponentsEnum.KWUARM.getPrimaryItemName());
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return null;
    }

    @Override
    public boolean isJumping() {
        return true;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }

    @Override
    public void logNode() {
        this.getClass().getSimpleName();
    }
}
