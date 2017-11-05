import entity.DFAEntity;
import exception.LexFileException;
import exception.RegularException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 53068 on 2017/10/30 0030.
 */
public class LexGenerator {

    private static LexGenerator generator;

    private LexExtractor extractor;
    private List<String> regulars;
    private List<int[]> state;      //合并后的dfa
    private List<String> rules;     //每条rule后面的模块，index+1等于该rule的优先级

    private LexGenerator(){

    }

    public static LexGenerator getGenerator(){
        if(generator == null){
            generator = new LexGenerator();
        }
        return generator;
    }

    public void compile(String path) throws Exception{
        this.extractor = new LexExtractor(path).extract();
        rules = extractor.getRules();
        regulars = extractor.getRegulars();

        List<DFAEntity> dfas = new ArrayList<>();
        for (int i = 0; i < regulars.size(); i++) {
            int[][] dfa = new REToNFA(regulars.get(i)).getNFA().getDFA();
            dfas.add(new DFAEntity(i+1, dfa));
        }
        state = new DFAMerger(dfas).merge();

        this.lexGenerate();
    }

    /**
     * 生成最终的词法分析器文件
     */
    private void lexGenerate(){

        File dir = new File("lexer");
        if(!dir.exists()){
            dir.mkdir();
        }

        String delim = "\t";
        StringBuilder stateTable = new StringBuilder();
        StringBuilder analyzerContent = new StringBuilder();
        String content = "";
        for(int i = 0; i < state.size(); i ++){
            for (int j = 0; j < state.get(0).length; j ++){
                stateTable.append(state.get(i)[j]);
                if(j < state.get(0).length - 1){
                    stateTable.append(" ");
                }
            }
            if(i < state.size() - 1){
                stateTable.append("\n");
            }
        }
        try {
            File sample = new File("src/sample/Analyzer.java");
            BufferedReader reader = new BufferedReader(new FileReader(sample));
            String line = reader.readLine(); //第一行去掉
            while ((line = reader.readLine()) != null){
                analyzerContent.append(line+"\n");
            }
            content = analyzerContent.toString().replace("/*this is state table*/", "=new int["+state.size()+"][128]");
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            String analyzer = new String("lexer/Analyzer.java");
            FileWriter writer = new FileWriter(analyzer);
            writer.write(content.toString());
            writer.flush();
            writer.close();

            writer = new FileWriter("lexer/state");
            writer.write(stateTable.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder rulesContent = new StringBuilder();
        for (int i = 0; i < rules.size(); i++) {
            rulesContent.append(delim+delim+delim+"case "+ (i+1)+":\n");
            rulesContent.append(rules.get(i)+"\n"+"break;"+"\n");
        }
        StringBuilder lexerContent = new StringBuilder();
        lexerContent.append(extractor.getHeader());
        try {
            File sample = new File("src/sample/Lexer.java");
            BufferedReader reader = new BufferedReader(new FileReader(sample));
            String line = reader.readLine(); //第一行去掉
            while ((line = reader.readLine()) != null){
                if(line.startsWith("/*this is user-rules area*/")){
                    lexerContent.append(rulesContent.toString());
                }else if(!line.startsWith("}")){    //最后的"}"去掉
                    lexerContent.append(line+"\n");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        lexerContent.append(extractor.getSubroutines()).append("\n}");
        try {
            String lexer = new String("lexer/Lexer.java");
            FileWriter writer = new FileWriter(lexer);
            writer.write(lexerContent.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
