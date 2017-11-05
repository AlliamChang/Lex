package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 53068 on 2017/10/25 0025.
 */
public class Node {

    private int state;
    private boolean isEndState;

    private List<Integer> outEdge; //E-edge is -1
    private List<Node> outNeighbor;

    public Node(int state){
        this(state, false);
    }

    public Node(int state, boolean isEndState){
        this.isEndState = isEndState;
        this.state = state;
        outEdge = new ArrayList<>();
        outNeighbor = new ArrayList<>();
    }

    /**
     * add the next node
     * @param nextEdge the char of next edge, if e-edge, that input -1
     * @param nextState
     * @return
     */
    public boolean addNode(int nextEdge, Node nextState){
        if(nextState == null)
            return false;
        this.outEdge.add(nextEdge);
        this.outNeighbor.add(nextState);
        return true;
    }

    public void setEndState(){
        this.isEndState = true;
    }

    public void cancelEndState(){
        this.isEndState = false;
    }

    public int getState() {
        return state;
    }

    public List<Integer> getOutEdge() {
        return outEdge;
    }

    public List<Node> getOutNeighbor() {
        return outNeighbor;
    }
}
