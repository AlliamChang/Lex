import entity.NFAEntity;
import entity.Node;

import java.util.*;

/**
 * Created by 53068 on 2017/10/24 0024.
 */
public class NFAToDFA {

    private NFAEntity nfa;
    private Set<Character> header;
    private int[][] eClosureTable; //e-closure table start from row 1
    private List<Set<Integer>> stateSets = new ArrayList<>();
    private List<Set<Node>> nodeSets = new ArrayList<>();
    public static final int CHAR_NUM = 128;
    public static final int LARGE_ROW = 1000;

    public NFAToDFA(NFAEntity nfa, Set<Character> header){
        this.header = header;
        this.nfa = nfa;
        this.eClosureTable = new int[LARGE_ROW][CHAR_NUM];
    }

    public void printTable(){
        String delim = "\t";
        System.out.print(delim);
        Set<Character> print = new HashSet<>(header);
        print.add('\0');

        for (Character c : print) {
            System.out.print(c + delim);
        }
        System.out.println();
        for (int i = 0; i < stateSets.size(); i ++){
            System.out.print((i+1) + delim);
            for (Character c : print) {
                System.out.print(eClosureTable[i+1][c] + delim);
            }
            System.out.println();
        }
    }

    public int[][] getDFA(){
        eClosureTable();
        simplify();
//        printTable();
        return eClosureTable;
    }

    private void eClosureTable(){
//        int state = 1;
        Set<Integer> firstClosure = new HashSet<>();
        Set<Node> nodeSet = new HashSet<>();

        //初始状态
        eClosure(nfa.getStart(), firstClosure, nodeSet);
        stateSets.add(firstClosure);
        nodeSets.add(nodeSet);

        int now = 0;
        while (now < stateSets.size()){
            for (Character edge : header) {
                //经过edge边得到的集合
                Map<Integer, Node> stateSet = nextSet(edge, nodeSets.get(now));
                //e-closure算法得到的集合
                nodeSet = new HashSet<>();
                Set<Integer> closureSet = new HashSet<>();
                for (Integer i : stateSet.keySet()) {
                    eClosure(stateSet.get(i), closureSet, nodeSet);
                }
                //检查已有状态，比较是否为新的状态
                int hasSame = hasTheSameClosure(closureSet);
                if(hasSame > 0){
                    eClosureTable[now+1][edge] = hasSame;
                }else if(closureSet.size() > 0){    //空集不记录
                    stateSets.add(closureSet);
                    nodeSets.add(nodeSet);
                    eClosureTable[now+1][edge] = stateSets.size();
                    //判断新状态是否为终态
                    if(closureSet.contains(nfa.getEnd().getState())){
                        eClosureTable[stateSets.size()][0] = -1;
                    }
                }
            }
            now ++;
        }
    }

    /**
     * 最小化DFA
     */
    private void simplify(){
        Set<Character> check = new HashSet<>(header);

        check.add('\0');
        for (int i = 1; i <= stateSets.size(); i++) {
            int j = i+1;
            for( ; j <= stateSets.size(); j ++){
                boolean isSame = true;
                for (Character c : check) {
                    if(eClosureTable[i][c] != eClosureTable[j][c]){
                        isSame = false;
                        break;
                    }
                }
                if(isSame){
//                    System.out.println(i +" <=> " +j);
                }
            }
        }
    }

    /**
     * 判断是否存在相同的闭包集
     * @param target
     * @return
     */
    private int hasTheSameClosure(Set<Integer> target){
        int result = 0;
        for (int i = 0; i < stateSets.size(); i++) {
            if(stateSets.get(i).equals(target)){
                result = i+1;
                break;
            }
        }
        return result;
    }

    /**
     * 推到经过edge边后的集合
     * @param edge
     * @param nodeSet
     * @return
     */
    private Map<Integer, Node> nextSet(char edge, Set<Node> nodeSet){
        Map<Integer, Node> result = new HashMap<>();
        for (Node node : nodeSet) {
            if(node.getOutEdge().contains(edge - '\0')){
                for (int i = 0; i < node.getOutEdge().size(); i++) {
                    if(node.getOutEdge().get(i) == edge - '\0'
                            && !result.containsKey(node.getOutNeighbor().get(i).getState())){
                        result.put(node.getOutNeighbor().get(i).getState(), node.getOutNeighbor().get(i));
                    }
                }
            }
        }
        return result;
    }

    private void eClosure(Node target, Set<Integer> closureSet, Set<Node> nodeSet){
        if(closureSet.contains(target.getState())){
            return;
        }else {
            closureSet.add(target.getState());
            nodeSet.add(target);
        }
        for (int i = 0; i < target.getOutEdge().size(); i++) {
            if(target.getOutEdge().get(i) == -1){
                eClosure(target.getOutNeighbor().get(i), closureSet, nodeSet);
            }
        }
    }
}
