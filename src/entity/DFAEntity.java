package entity;

/**
 * Created by 53068 on 2017/11/4 0004.
 */
public class DFAEntity {

    private int priority;
    public int[][] dfa;

    public DFAEntity(int priority ){
        this.priority = priority;
    }

    public DFAEntity(int priority, int[][] dfa){
        this.priority = priority;
        this.dfa = dfa;
    }

    public int getPriority(){
        return this.priority;
    }

}
