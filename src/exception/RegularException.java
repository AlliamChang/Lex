package exception;

/**
 * Created by 53068 on 2017/10/25 0025.
 */
public class RegularException extends Exception{

    public RegularException(){
        super("Error format in regular expression");
    }
}
