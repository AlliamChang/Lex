package exception;

/**
 * Created by 53068 on 2017/10/30 0030.
 */
public class LexFileException extends Exception{

    public LexFileException(int line){
        super("Error format in lex file at line " + line);
    }

    public LexFileException(){
        super("Error format in lex file! ");
    }

    public LexFileException(String details){
        super(details);
    }
}
