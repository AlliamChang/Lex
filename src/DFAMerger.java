import entity.DFAEntity;

import java.util.*;

/**
 * Created by 53068 on 2017/11/4 0004.
 */
public class DFAMerger {

    private List<Map<Integer, Integer>> mergeState = new ArrayList<>(); //key为原dfa优先级 , value为原dfa状态名
    private List<DFAEntity> allDfa = new ArrayList<>();
    private List<Integer[]> mergeEndState = new ArrayList<>();
    private List<int[]> newDfa = new ArrayList<>();

    /**
     * list中的dfa排序应为原RE优先级的排序
     * @param allDfa
     */
    public DFAMerger(List<DFAEntity> allDfa){
        this.allDfa = allDfa;
    }

    /** 对每个dfa进行merge是采用合并但不重复的方法，类似于nfa=>dfa的过程
     * 对于A: ab 和 B: (a|b)* 两个RE的合并且A优先级高于B
     * e.g.  A   0  a  b    B   0  a  b
     *       1   0  2  0    1   0  2  2
     *       2   0  0  3    2  -1  2  2
     *       3  -1  0  0
     *
     *     Ii         0       a               b
     *  I0={A1,B1}    0   I1={A2,B2}       I2={B2}
     *  I1={A2,B2}    B      I2           I3={A3, B2}
     *  I2={B2}       B      I2              I2
     *  I3={A3,B2}   A,B     I2              I2
     *
     *  最后的表格为(第0列为最高优先级的终态):
     *      0   a   b
     *  0   0   1   2
     *  1  -2   2   3
     *  2  -2   2   2
     *  3  -1   2   2
     */
    public List<int[]> merge(){
        //初始状态I0 = {每个dfa的起点}
        //每个dfa都是从row 1开始
        Map<Integer, Integer> firstMergeState = new HashMap<>();
        allDfa.forEach(dfaEntity -> {
            firstMergeState.put(dfaEntity.getPriority(), 1);
        });
        mergeState.add(firstMergeState);

        for (int i = 0; i < mergeState.size(); i ++){
//            if(i > 4){
//                break;
//            }
            int[] header = new int[NFAToDFA.CHAR_NUM];
            List<Integer> endState = new ArrayList<>();

            //看该新的闭包集中有哪些状态原本属于终态
            Iterator<Map.Entry<Integer, Integer>> it = mergeState.get(i).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Integer> state = it.next();
                if (allDfa.get(state.getKey() - 1).dfa[state.getValue()][0] == -1) {
                    header[0] = -1;
                    endState.add(state.getKey());
                }
            }

            for(int j = 1; j < NFAToDFA.CHAR_NUM; j ++) {

                //对Ii中的每个状态进行规约
                Map<Integer, Integer> eachMergeState = new HashMap<>();
                it = mergeState.get(i).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Integer> state = it.next();
                    if (allDfa.get(state.getKey() - 1).dfa[state.getValue()][j] > 0) {
                        eachMergeState.put(state.getKey(), allDfa.get(state.getKey() - 1).dfa[state.getValue()][j]);
                    }
                }

                //按照nfa=>dfa的方法进行规约
                if(eachMergeState.size() > 0) {
                    if (mergeState.contains(eachMergeState)) {
                        header[j] = mergeState.indexOf(eachMergeState);
                    } else {
                        mergeState.add(eachMergeState);
                        header[j] = mergeState.size() - 1;
                    }
                }
            }
            if(header[0] == -1){
                Integer[] temp = new Integer[endState.size()];
                endState.toArray(temp);
                mergeEndState.add(temp);
            }
            newDfa.add(header);
        }

        //为每个新的终态导向优先级最高的终态
        int j = 0;
        for(int i = 0; i < newDfa.size(); i ++){
            if(newDfa.get(i)[0] == -1){
                newDfa.get(i)[0] = - highestPriority(mergeEndState.get(j));
                j ++;
            }
        }

        return newDfa;
    }

    public void print(){
        String delim = "\t";
        System.out.print(delim);

        for (int i = 0; i < NFAToDFA.CHAR_NUM; i ++) {
            System.out.print( i + delim);
        }
        System.out.println();
        for (int i = 0; i < newDfa.size(); i ++){
            System.out.print((i) + delim);
            for(int j = 0; j < newDfa.get(i).length; j ++){
                System.out.print(newDfa.get(i)[j] + delim);
            }
            System.out.println();
        }
    }

    private int highestPriority(Integer[] ends){
        int highest = ends[0];

        if(ends.length > 1){
            for (Integer end : ends) {
                if(end < highest){
                    highest = end;
                }
            }
        }

        return highest;
    }
}
