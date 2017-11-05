package sample;//需要去掉

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Lexer {

    public static String text;

    public static void rule(int ruleNum){
        switch (ruleNum){
/*this is user-rules area*/
            default:
System.out.println("Unknow rule number: " + ruleNum);
        }

    }

    public static void main(String[] args){
        Analyzer.readTable();
        System.out.print("Please input the file to be analyzed: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(new File(input)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null){
                sb.append(line+"\n");
            }
            Analyzer.analyze(sb.toString().toCharArray());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}