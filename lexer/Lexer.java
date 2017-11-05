



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Lexer {

    public static String text;

    public static void rule(int ruleNum){
        switch (ruleNum){
			case 1:
System.out.println("white space");
break;
			case 2:
/* do nothing */
break;
			case 3:
token("if");
break;
			case 4:
token("else");
break;
			case 5:
token("while");
break;
			case 6:
token("for");
break;
			case 7:
token("int");
break;
			case 8:
token("float");
break;
			case 9:
token("id");
break;
			case 10:
token("number");
break;
			case 11:
token("decimal");
break;
			case 12:
token("relop");
break;
			case 13:
token("relop");
break;
			case 14:
token("relop");
break;
			case 15:
token("relop");
break;
			case 16:
token("relop");
break;
			case 17:
token("operator");
break;
			case 18:
token("operator");
break;
			case 19:
token("operator");
break;
			case 20:
token("operator");
break;
			case 21:
token("operator");
break;
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

public static void token(String tag){
    System.out.println("<"+text+", "+tag+">");
}

}