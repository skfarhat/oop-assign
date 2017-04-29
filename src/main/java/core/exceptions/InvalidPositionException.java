package core.exceptions;

// TODO:this should be unchecked (just like ArrayIndexOutOfBounds)
public class InvalidPositionException extends LifeRuntimeException {

    /**  default constructor */
    public InvalidPositionException() {super(); };

    /**  constructor with String message */
    public InvalidPositionException(String e) { super(e); };
}
