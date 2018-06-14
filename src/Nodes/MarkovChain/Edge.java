package Nodes.MarkovChain;

import java.util.concurrent.ThreadLocalRandom;

public class Edge {
    private Class<? extends ExecutableNode> v;
    private int defaultExecutionWeight;
    private int currentExecutionWeight;

    public Edge(Class<? extends ExecutableNode> v, int defaultExecutionWeight) {
        this.v = v;
        this.defaultExecutionWeight = defaultExecutionWeight;
        randomizeExecutionWeight();
    }

    public Class<? extends ExecutableNode> getV() {
        return v;
    }

    public int getCurrentExecutionWeight() {
        return currentExecutionWeight;
    }

    public void randomizeExecutionWeight() {
        currentExecutionWeight = ThreadLocalRandom.current().nextInt((int) Math.ceil(defaultExecutionWeight * 0.8),  (int) Math.ceil(defaultExecutionWeight * 1.2));
    }
}
