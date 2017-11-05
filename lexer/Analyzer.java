
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Stack;

public class Analyzer {

    private static int[][] stateTable=new int[208][128];
    private static Stack<Integer> cache = new Stack<>();
    private static Stack<Character> textCache = new Stack<>();

    public static void readTable(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("state")));
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null){
                String[] num = line.trim().split(" ");
                for (int j = 0; j < num.length; j++) {
                    stateTable[i][j] = Integer.valueOf(num[j]);
                }
                i ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void analyze(char[] input){
        int nowState = 0;
        for(int i = 0; i < input.length; i ++){
            cache.push(nowState);
            textCache.push(input[i]);
            nowState = stateTable[nowState][input[i]];
            if(nowState == 0){
                while (cache.size() > 1){
                    i --;
                    if(stateTable[cache.peek()][0] < 0){  //if end, mean match
                        Lexer.text = "";
                        textCache.pop();
                        while (!textCache.empty()){
                             Lexer.text = textCache.pop() + Lexer.text;
                        }
                        //turn into user's rules
                        Lexer.rule( - stateTable[cache.peek()][0]);
                        cache.clear();
                        nowState = 0;
                        break;
                    }else {                               //if not end, go back one character
                        cache.pop();
                        textCache.pop();
                    }
                }
                //no match that print it directly
                if(cache.size() == 1){
                    System.out.println(input[i]);
                    cache.clear();
                    textCache.clear();
                }
            }
        }
    }
}
