package com.perago.techtest;

/**
 * Implement or modify this class as you see fit
 *
 */
public class DiffException extends Exception {

    private static final long serialVersionUID = -7698912729249813850L;

    //Catch Exception and passto java.lang.Exception
    public DiffException(String message) {
        super(message);
    }

}
