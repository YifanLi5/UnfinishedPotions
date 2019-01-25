package Nodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;

import java.util.List;


public class DebuggingNode implements ExecutableNode {


    @Override
    public boolean canExecute() throws InterruptedException {
        return false;
    }

    @Override
    public int executeNode() throws InterruptedException {
        return 0;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return null;
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

    }
}
