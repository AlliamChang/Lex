import entity.NFAEntity;
import entity.Node;
import exception.RegularException;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by 53068 on 2017/10/24 0024.
 */
public class REToNFA {

    private String regularExp;
    private Set<Character> header = new HashSet<>();
    private NFAEntity nfa;

    /**
     * [, ], (, ), {, }, +, *, \, |, -
     * @param regularExp
     */
    public REToNFA(String regularExp){
        this.regularExp = regularExp;
    }

    public NFAToDFA getNFA() throws RegularException{
        postfix();
        convertToNFA();
        return new NFAToDFA(nfa, header);
    }


    /**
     * convert to expression in postfix
     * high  ()
     *   \   *
     *   \   .
     * low   |
     */
    private void postfix() throws RegularException{
        Stack<Character> operator = new Stack<>();
        StringBuilder postfixExp = new StringBuilder();
        char[] chars = ( "("+regularExp+")" ).toCharArray();

        for (int i = 0; i < chars.length; i ++) {
            switch (chars[i]){
                case '\\':
                    if(i < chars.length - 1){
                        if(chars[i+1] == 'n'){
                            postfixExp.append('\n');
                            i ++;
                        }else if(chars[i+1] == 't'){
                            postfixExp.append('\t');
                            i ++;
                        }else {
                            postfixExp.append(chars[i]).append(chars[++i]);
                        }

                    }else {
                        throw new RegularException();
                    }
                    break;
                case '(':
                    operator.push(chars[i]);
                    break;
                case '*':
                    while (operator.peek() == '*'){
                        postfixExp.append(operator.pop());
                    }
                    operator.push(chars[i]);
                    break;
                case '.':
                    while (operator.peek() == '*' || operator.peek() == '.'){
                        postfixExp.append(operator.pop());
                    }
                    operator.push(chars[i]);
                    break;
                case '|':
                    while (operator.peek() == '*' || operator.peek() == '.' || operator.peek() == '|'){
                        postfixExp.append(operator.pop());
                    }
                    operator.push(chars[i]);
                    break;
                case ')':
                    while (operator.peek() != null && operator.peek() != '('){
                        postfixExp.append(operator.pop());
                    }
                    if(operator.pop() != '('){
                        throw new RegularException();
                    }
                    break;
                default:
                    postfixExp.append(chars[i]);
            }
        }

        regularExp = postfixExp.toString();
//        System.out.println(regularExp);
    }

    private void convertToNFA() throws RegularException{
        char[] chars = regularExp.toCharArray();
        Stack<NFAEntity> stack = new Stack<>();
        int nextState = 1;

        for (int i = 0; i < chars.length; i++) {
            if(chars[i] == '.') {
                NFAEntity behind = stack.pop();
                NFAEntity front = stack.pop();
                if (behind == null || front == null) {
                    throw new RegularException();
                }
                //there is no new state
                // -> 1 -> 2 -> 3 -> 4.
                //      a    e    b
                front.getEnd().addNode(-1, behind.getStart());
                front.getEnd().cancelEndState();

                stack.push(new NFAEntity(front.getStart(), behind.getEnd()));
            }else if (chars[i] == '|') {
                NFAEntity behind = stack.pop();
                NFAEntity front = stack.pop();
                if (behind == null || front == null) {
                    throw new RegularException();
                }
                //there are two new state(5, 6)
                //      -> 1 -> 2 ->
                // -> 5 -> 3 -> 4 -> 6.
                Node newBegin = new Node(nextState++);
                newBegin.addNode(-1, front.getStart());
                newBegin.addNode(-1, behind.getStart());

                Node newEnd = new Node(nextState++, true);
                front.getEnd().addNode(-1, newEnd);
                front.getEnd().cancelEndState();
                behind.getEnd().addNode(-1, newEnd);
                behind.getEnd().cancelEndState();

                stack.push(new NFAEntity(newBegin, newEnd));
            }else if(chars[i] == '*') {
                NFAEntity self = stack.pop();
                if(self == null){
                    throw new RegularException();
                }
                //there are two new state
                // -> 3 -> 1 -> 2 -> 4.
                //          <--
                //     ------------>
                Node newBegin = new Node(nextState++);
                Node newEnd = new Node(nextState++, true);
                newBegin.addNode(-1, newEnd);
                newBegin.addNode(-1, self.getStart());
                self.getEnd().cancelEndState();
                self.getEnd().addNode(-1, self.getStart());
                self.getEnd().addNode(-1, newEnd);

                stack.push(new NFAEntity(newBegin, newEnd));
            } else if (chars[i] == '\\') {
                if(i < chars.length - 1){
                    header.add(chars[++i]);
                    Node start = new Node(nextState++);
                    Node end = new Node(nextState++, true);
                    start.addNode(chars[i], end);
                    stack.push(new NFAEntity(start, end));
                }else {
                    throw new RegularException();
                }
            } else {
                header.add(chars[i]);
                Node start = new Node(nextState++);
                Node end = new Node(nextState++, true);
                start.addNode(chars[i], end);
                stack.push(new NFAEntity(start, end));
            }
        }

        nfa = stack.pop();
        nfa.setNumOfState(nextState - 1);
    }
}
