package Nodes.MarkovChain;

import java.util.List;

public interface ExecutableNode {
    boolean canExecute() throws InterruptedException;

    int executeNode() throws InterruptedException; //return anything < 0 to stop script

    List<Edge> getAdjacentNodes();

    boolean isJumping();//used by MarkovNodeExecutor to indicate whether a special node traversal is requested

    Class<? extends ExecutableNode> setJumpTarget();  //if isJumping() specify the class jump target

    void logNode();
}
