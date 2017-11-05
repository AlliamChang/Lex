package entity;

/**
 * Created by 53068 on 2017/10/25 0025.
 */
public class NFAEntity {

    private Node start;
    private Node end;
    private int numOfState;

    public NFAEntity(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public int getNumOfState() {
        return numOfState;
    }

    public void setNumOfState(int numOfState) {
        this.numOfState = numOfState;
    }
}
