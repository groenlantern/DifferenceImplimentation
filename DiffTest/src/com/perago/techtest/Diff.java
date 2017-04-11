package com.perago.techtest;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The object representing a diff.
 * Implement this class as you see fit. 
 *
 * @param <T>
 * 
 * Modified : Jean-Pierre Erasmus 
 */
public class Diff<T extends Serializable> {
    
    /**Variables
     * 
     */    
    private ArrayList<DiffField<T>> fieldData = null;

    /**
     * @return the fieldData
     */
    public ArrayList<DiffField<T>> getFieldData() {
        return fieldData;
    }

    /**
     * @param fieldData the fieldData to set
     */
    public void setFieldData(ArrayList<DiffField<T>> fieldData) {
        this.fieldData = fieldData;
    }
 
}
