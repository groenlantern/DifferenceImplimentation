/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package difftest;

import com.perago.techtest.Diff;
import com.perago.techtest.DiffException;
import com.perago.techtest.DiffField;
import com.perago.techtest.DiffRenderer;
import java.util.ArrayList;

/**
 *
 * @author Jean-Pierre Erasmus
 */
public class DiffRenderImp implements DiffRenderer {

    private final static String TABC = "\t";
    
    /**
     * 
     * @param diff
     * @return
     * @throws DiffException 
     */
    @Override
    public String render(Diff<?> diff) throws DiffException {
          
        try {                           
            if ( diff.getFieldData() == null) { 
                return "No Differences Found\n";
            }
            
            return recsivePrint("1", (ArrayList<DiffField<?>>) diff.getFieldData(), "");
        } catch (Exception ex1) { 
            throw new DiffException("Difference Calculate Exception 10011 : " + ex1.getMessage());
        } 
                
    }

    /**
     * 
     * @param prefixCounter
     * @param fieldDataArray
     * @param tabChar
     * @return 
     */
    public static String recsivePrint(String prefixCounter,  ArrayList<DiffField<?>> fieldDataArray, String tabChar) { 
        int cntr = 0;
        String tabs = tabChar;
        String cntrPrint = "";
        String outputString = "";
        
        for ( DiffField<?> perObj : fieldDataArray ) { 
            String typeDesc = "";
            String opDesc = "";
            //Process different tran types
            if ( perObj.isIsCreate()) { 
                typeDesc = " Create"; 
                if ( perObj.getChildDiffMod() == null) { 
                    opDesc = " as \"" + perObj.getChildValueMod() + "\"";
                }
            } 
            if ( perObj.isIsUpdate()) { 
                typeDesc = " Update"; 
                if ( perObj.getChildDiffMod() == null) { 
                    opDesc = " from \"" + perObj.getChildValueOrig() + "\" to \"" + perObj.getChildValueMod() + "\"";
                }
            } 
            if ( perObj.isIsDelete()) { 
                typeDesc = " Delete";                
            } 
                        
            //Setup indentation etc
            if (cntr == 1) tabs += TABC;            
            if (cntr > 0) cntrPrint = "." + cntr;                
            if (cntr == 0) opDesc = "";                
            
            //Output row data
            outputString += (prefixCounter + cntrPrint + tabs + typeDesc + ":" + perObj.getName() + opDesc + "\n");
            
            //Retrieve child row data
            if ( perObj.getChildDiffMod() != null) { 
                outputString += ( recsivePrint(( prefixCounter + cntrPrint ), (ArrayList<DiffField<?>>) perObj.getChildDiffMod(), (tabs + TABC) ));
             }
                        
            cntr++;
        }
        
        
        return outputString;
    }
    
   
    
    
}
