import exception.LexFileException;
import exception.RegularException;

import java.io.*;
import java.util.*;

/**
 * Created by 53068 on 2017/10/30 0030.
 */
public class LexExtractor {

    private File lexFile;

    private StringBuilder header = new StringBuilder();
    private Map<String, String> regularExp = new HashMap<>();   //变量名, 变量的正则表达式定义
    private StringBuilder rulesContent = new StringBuilder();
    private StringBuilder subroutines = new StringBuilder();

    private List<String> regulars = new ArrayList<>();      //所有的正则表达式, index+1等于该rule的优先级
    private List<String> rules = new ArrayList<>();         //用户定义的规则内容，index+1等于该rule的优先级

    /**
     *  file format:
     *  {definitions}
     *  %%         // this part
     *  {rules}    // must exist
     *  %%
     *  {user subroutines}
     */
    public LexExtractor(String filePath){
        lexFile = new File(filePath);
    }

    public LexExtractor extract() throws FileNotFoundException, LexFileException{
        if(!lexFile.exists()){
            throw new FileNotFoundException();
        }
        BufferedReader reader = new BufferedReader(new FileReader(lexFile));
        String line;
        int lineNum = 0, part = 0;

        try {
            while ((line = reader.readLine()) != null){
                lineNum ++;
                if(line.trim().equals("%%")){
                    part ++;
                }else if(line.trim().startsWith("%{")){
                    header.append(line.replace("%{", "") + "\n");
                    while ((line = reader.readLine()) != null){
                        lineNum ++;
                        if(line.trim().endsWith("%}")){
                            header.append(line.replace("%}", "") + "\n");
                            break;
                        }else {
                            header.append(line + "\n");
                        }
                    }
                    if(line == null){
                        throw new LexFileException(lineNum);
                    }
                }else {
                    if(part == 0){
                        if(line.trim().length() > 0) {
                            String[] temp = line.trim().split(" +");
                            if(temp.length > 1){
                                regularExp.put(temp[0], standardize(temp[1]));
                            }else {
                                throw new LexFileException(lineNum);
                            }
                        }
                    }else if(part == 1){
                        if(line.trim().length() > 0) {
                            rulesContent.append(line + "\n");
                        }
                    }else if(part == 2){
                        subroutines.append(line + "\n");
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println("------------header------------");
//        System.out.println(header.toString());
//        System.out.println("------------regular------------");
//        regularExp.entrySet().forEach(en -> {
//            System.out.println(en.getKey() + " " + en.getValue());
//        });
//        System.out.println("------------rules------------");
//        System.out.println(rulesContent.toString());
//        System.out.println("------------subroutines------------");
//        System.out.println(subroutines.toString());

        this.handleContent();

        return this;
    }



    /**
     * standardize the regular expression
     * {abc}: 变量名，采取直接复制的方法
     * [abc]: 以(a|b|c)代替
     * a-c: 以(abc)代替
     * a+: 以aa*代替
     */
    private String standardize(String nonStandard) throws LexFileException {
        Stack<String> stack = new Stack<>();
        String standardExp = "";
        char[] element = nonStandard.toCharArray();

        for (int i = 0; i < element.length; i++) {
            if(element[i] == '[' || element[i] == '('){
                stack.push(String.valueOf(element[i]));
            }else if(element[i] == ']'){
                String array = "[".equals(stack.peek())? "":stack.pop();
                while ( !"[".equals(stack.peek())){
                    array = stack.pop() + "|" + array;
                }
                stack.pop();
//                char[] each = array.toCharArray();
//                array = "";
//                for (int i1 = 0; i1 < each.length; i1++) {
//                    if(i1 < each.length - 1){
//                        array += each[i1] + "|";
//                    }else {
//                        array += each[i1];
//                    }
//                }
                stack.push("("+array+")");
            }else if(element[i] == ')'){
                String array = ")";
                while (!"(".equals(stack.peek())){
                    array = stack.pop() + array;
                }
                array = stack.pop() + array;
                stack.push(array);
            }else if(element[i] == '+'){
                String more = stack.pop();
                more = more + more + "*";
                stack.push(more);
            }else if(element[i] == '-'){
                if(i < element.length - 1 && stack.peek() != null){
                    char[] start = stack.pop().toCharArray();
                    if(start.length == 1){
                        if(start[0] < element[i+1]){
                            for(int j = 0; j <= (element[i+1] - start[0]); j ++){
                                char c = start[0];
                                stack.push(String .valueOf((char)(start[0]+j)));
                            }
                            i ++;
                        }else {
                            throw new LexFileException("Error format in regular expression");
                        }
                    }else {
                        throw new LexFileException("Error format in regular expression");
                    }
                }else {
                    throw new LexFileException("Error format in regular expression");
                }
            }else if(element[i] == '\\'){
                if(i < element.length - 1){
                    stack.push(String.valueOf(new char[]{element[i], element[++i]}));
                }else {
                    throw new LexFileException("Error format in regular expression");
                }
            }else if(element[i] == '{'){
                String id = "";
                i ++;
                while (element[i] != '}'){
                    id += element[i];
                    i ++;
                }
                if(regularExp.containsKey(id)){
                    stack.push(regularExp.get(id));
                }else {
                    throw new LexFileException("No declared variable {" + id+"}");
                }
            }else {
                stack.push(String .valueOf(element[i]));
            }
        }
        while (!stack.empty()){
            standardExp = stack.pop() + standardExp;
        }

        /*
        these cases should add a '.':
        ab, a(, a*b, a*(, )(, )a
         */
        String plusAdded = "";
        char[] each = standardExp.toCharArray();
        for (int i = 0; i < each.length; i++) {
            plusAdded += each[i];
            if(each[i] == '\\'){
                if(i < each.length - 2){
                    if(each[i+2] == '(' || (each[i+2] != '|' && each[i+2] != '*' && each[i+2] != '(' && each[i+2] != ')')){
                        plusAdded += each[++i] + ".";
                    }
                }else if(i == each.length - 1){
                    throw new LexFileException();
                }
            }else if(each[i] == '*' || each[i] == ')' ||
                    (each[i] != '|' && each[i] != '*' && each[i] != '(' && each[i] != ')')){
                if(i < each.length - 1) {
                    if (each[i + 1] == '(' || (each[i + 1] != '|' && each[i + 1] != '*' && each[i + 1] != '(' && each[i + 1] != ')')) {
                        plusAdded += ".";
                    }
                }
            }
        }
//        System.out.println(plusAdded);
        return plusAdded;
    }

    private void handleContent() throws LexFileException{
        int tag = 0;
        char[] input = rulesContent.toString().toCharArray();

        for (int i = 0; i < input.length; i++) {
            if(input[i] == '{'){
                i ++;
                if(tag == 0){
                    String id = "";
                    while (input[i] != '}'){
                        id += input[i];
                        if(i < input.length - 1){
                            i ++;
                        }else {
                            throw new LexFileException();
                        }
                    }

                    if(regularExp.containsKey(id)) {
                        regulars.add(regularExp.get(id));
                    }else {
                        throw new LexFileException("No declared variable {" + id+"}");
                    }
                    i ++;
                    tag = 1;
                }else{
                    String rule = "";
                    while (input[i] != '}'){
                        if(input[i] == '\\' && i < input.length && input[i+1] == '}'){
                            rule += input[i] + input[++i];
                        }else {
                            rule += input[i];
                        }
                        if(i < input.length - 1){
                            i ++;
                        }else {
                            throw new LexFileException();
                        }
                    }
                    rules.add(rule);
                    i ++;
                    tag = 0;
                }
            }else if(input[i] == '"'){
                String id = "";
                i ++;
                while (input[i] != '"'){
                    id += "\\"+input[i];
                    if(i < input.length - 1){
                        i ++;
                    }else {
                        throw new LexFileException();
                    }
                }
                regulars.add(standardize(id));
                tag = 1;
            }else if(input[i] != ' ' && input[i] != '\t' && input[i] != '\n') {
                String id = "";
                while (input[i] != ' '){
                    id += input[i];
                    if(i < input.length - 1){
                        i ++;
                    }else {
                        throw new LexFileException("Error occured index " + i + " of " + (input.length-1));
                    }
                }
                regulars.add(standardize(id));
                i ++;
                tag = 1;
            }
        }

        regulars.forEach(s -> System.out.println(s));
    }

    public StringBuilder getHeader() {
        return header;
    }

    public List<String> getRegulars() {
        return regulars;
    }

    public List<String> getRules() {
        return rules;
    }

    public StringBuilder getSubroutines() {
        return subroutines;
    }

}
