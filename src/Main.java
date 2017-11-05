import entity.DFAEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by 53068 on 2017/10/24 0024.
 */
public class Main {

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter your lex file: ");
        String lexFile = scanner.nextLine();
        try {
            LexGenerator.getGenerator().compile(lexFile);
            System.out.println("-----complete !-----");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
