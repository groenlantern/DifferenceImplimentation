/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package difftest;

import com.perago.techtest.Diff;
import com.perago.techtest.DiffEngine;
import com.perago.techtest.DiffException;
import com.perago.techtest.DiffField;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jeanpe
 */
public class DiffEngineImp implements DiffEngine {

    /**
     *
     * @param <T>
     * @param original
     * @param modified
     * @return
     * @throws DiffException
     */
    @Override
    public <T extends Serializable> Diff<T> calculate(T original, T modified) throws DiffException {

        try {
            Diff<T> diffObject = new Diff<>();

            if (modified == null) {
                if (original != null) {
                    //Deleted Object
                    //Field array
                    ArrayList<DiffField<T>> fieldData = new ArrayList<>();

                    //Single field object
                    DiffField<T> diffFieldOb = new DiffField<>();

                    //Create Main class diff object
                    diffFieldOb.setName(original.getClass().getSimpleName());
                    diffFieldOb.setIsParentClass(true);
                    diffFieldOb.setIsSimpleField(false);
                    diffFieldOb.setIsCreate(false);
                    diffFieldOb.setIsUpdate(false);
                    diffFieldOb.setIsDelete(true);
                    diffFieldOb.setChildValueOrig(null);
                    diffFieldOb.setChildDiffMod(null);
                    diffFieldOb.setChildValueMod(null);
                    fieldData.add(diffFieldOb);

                    diffObject.setFieldData(fieldData);
                    
                    return diffObject;
                }
            }

            //Created new Object output create statements 
            if (original == null) {
                if (modified == null) {
                    //No Change both null
                    diffObject.setFieldData(null);

                    return diffObject;
                }
                //Creat diff for create object
                diffObject.setFieldData(recursiveObjectCreate(null, modified));
            } else if (original.equals(modified)) {
                //No Change both the same
                diffObject.setFieldData(null);
            } else {
                //Updated Item 
                diffObject.setFieldData(recursiveObjectUpdate(null, original, modified));
            }

            return diffObject;

        } catch (Exception ex1) {
            throw new DiffException("Difference Calculate Exception 1001 : " + ex1.getMessage());
        }

    }

    /**
     *
     * @param classObj
     * @return
     */
    public static boolean isClassCollection(Class classObj) {
        return Collection.class.isAssignableFrom(classObj) || Map.class.isAssignableFrom(classObj);
    }

    /**
     *
     * @param objectParam
     * @return
     */
    public static boolean isCollection(Object objectParam) {
        return objectParam != null && isClassCollection(objectParam.getClass());
    }

    /**
     *
     * @param <T>
     * @param currentPropertyObject
     * @return
     * @throws DiffException
     */
    public static <T extends Serializable> ArrayList<DiffField<T>> recursiveObjectCreate(Object hashObj, Object currentPropertyObject) throws DiffException {
        try {
            //Field array
            ArrayList<DiffField<T>> fieldData = new ArrayList<>();

            //Single field object
            DiffField<T> diffFieldOb = new DiffField<>();

            //Create Main class diff object
            diffFieldOb.setName(currentPropertyObject.getClass().getSimpleName());
            diffFieldOb.setIsParentClass(true);
            diffFieldOb.setIsSimpleField(false);
            diffFieldOb.setIsCreate(true);
            diffFieldOb.setIsUpdate(false);
            diffFieldOb.setIsDelete(false);
            diffFieldOb.setClassName(currentPropertyObject.getClass());
            diffFieldOb.setChildValueOrig(null);
            diffFieldOb.setChildDiffMod(null);
            diffFieldOb.setChildValueMod(null);
            fieldData.add(diffFieldOb);

            //Get fields / variables 
            Field[] modifiedFields = currentPropertyObject.getClass().getDeclaredFields();

            //Loop property fields of object
            for (Field modProperty : modifiedFields) {
                //Skipped fields types
                if (modProperty.isSynthetic()
                        || modProperty.isEnumConstant()) {
                    continue;
                }
                //Value of property                
                Object value = null;
                try {
                    //public field
                    value = modProperty.get(currentPropertyObject);
                } catch (IllegalArgumentException | IllegalAccessException e1) {
                    //private field with accessor
                    try {
                        String getterName = "get" + modProperty.getName().substring(0, 1).toUpperCase() + modProperty.getName().substring(1);
                        Method objgetter = currentPropertyObject.getClass().getMethod(getterName);

                        value = objgetter.invoke(currentPropertyObject);
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
                        //Field not accessable
                    }
                }
                //Property is complex class, i.e. proprty is same type of parent/current object
                //Recursive call to expand this property - if null ignore - this is for newly created objects     
                //if (modProperty.getType().getCanonicalName().trim().equals(currentPropertyObject.getClass().getTypeName().trim())) {
                if ( !modProperty.getType().getCanonicalName().trim().startsWith("java.lang") && !(isCollection(value)) ) {
                    diffFieldOb = new DiffField<>();
                    diffFieldOb.setName(modProperty.getName());
                    diffFieldOb.setIsParentClass(false);
                    diffFieldOb.setIsSimpleField(false);
                    diffFieldOb.setIsCreate(true);
                    diffFieldOb.setIsUpdate(false);
                    diffFieldOb.setIsDelete(false);
                    diffFieldOb.setChildValueOrig(null);
                    diffFieldOb.setChildValueMod(null);

                    if (value != null) {
                        //Recursive call to expand this property - if null ignore - this is for newly created objects     
                        if ( hashObj != value ) { 
                            diffFieldOb.setChildDiffMod(recursiveObjectCreate(currentPropertyObject, value));
                            fieldData.add(diffFieldOb);
                        }
                    }
                    continue;
                }

                if (value == null) {
                    continue; //Value not accessable
                }
                //Collection types
                if (isCollection(value)) {
                    //Collection or Map Object
                    //Collection/Map property name text
                    diffFieldOb = new DiffField<>();

                    diffFieldOb.setName(modProperty.getName());
                    diffFieldOb.setIsParentClass(false);
                    diffFieldOb.setIsSimpleField(false);
                    diffFieldOb.setIsCreate(true);
                    diffFieldOb.setIsUpdate(false);
                    diffFieldOb.setIsDelete(false);
                    diffFieldOb.setChildDiffMod(null);

                    String listValues = "{";

                    //Collections
                    if (Collection.class.isAssignableFrom(value.getClass())) {
                        boolean isFirst = true;
                        //Loop through all objects of collection object
                        for (Object collectionObject : (Collection) value) {

                            //Add comma after first entry
                            if (!isFirst) {
                                listValues += ",";
                            }
                            listValues += "\"" + collectionObject.toString() + "\"";
                            isFirst = false;
                        }
                    } else if (Map.class.isAssignableFrom(value.getClass())) {
                        //Maps 
                        Iterator mapItr = ((Map) value).entrySet().iterator();
                        boolean isFirst = true;

                        //Loop through all objects of map object
                        while (mapItr.hasNext()) {
                            Map.Entry thisMapEl = (Map.Entry) mapItr.next();

                            Object objKey = thisMapEl.getKey();
                            Object objValue = thisMapEl.getValue();

                            //Add comma after first entry
                            if (!isFirst) {
                                listValues += ",";
                            }
                            listValues += "\"" + objKey.toString() + "\"=\"" + objValue + "\"";
                            isFirst = false;
                        }
                    }
                    listValues += "}";

                    if (!listValues.equals("{}")) {
                        diffFieldOb.setChildValueOrig(null);
                        diffFieldOb.setChildValueMod(listValues);
                        fieldData.add(diffFieldOb);
                    }
                } else {
                    //Simple Object - String etc.
                    diffFieldOb = new DiffField<>();

                    diffFieldOb.setName(modProperty.getName());
                    diffFieldOb.setIsParentClass(false);
                    diffFieldOb.setIsSimpleField(true);
                    diffFieldOb.setIsCreate(true);
                    diffFieldOb.setIsUpdate(false);
                    diffFieldOb.setIsDelete(false);
                    diffFieldOb.setChildValueOrig(null);
                    diffFieldOb.setChildDiffMod(null);
                    diffFieldOb.setChildValueMod(value);
                    fieldData.add(diffFieldOb);
                }

            }

            return fieldData;
        } catch (Exception ex1) {
            throw new DiffException("Recursive Loop Create 65005 : " + ex1.getMessage());
        }
    }

    /**
     *
     * @param <T>
     * @param origPropertyObject
     * @param modPropertyObject
     * @return
     * @throws DiffException
     */
    public static <T extends Serializable> ArrayList<DiffField<T>> recursiveObjectUpdate(Object hashObject, Object origPropertyObject, Object modPropertyObject) throws DiffException {
        try {
            //Field array
            ArrayList<DiffField<T>> fieldData = new ArrayList<>();

            //Single field object
            DiffField<T> diffFieldOb = new DiffField<>();

            //Updated Main class diff object - if updated will always exist
            diffFieldOb.setName(modPropertyObject.getClass().getSimpleName());
            diffFieldOb.setIsParentClass(true);
            diffFieldOb.setIsSimpleField(false);
            diffFieldOb.setIsCreate(false);
            diffFieldOb.setIsUpdate(true);
            diffFieldOb.setIsDelete(false);
            diffFieldOb.setChildValueOrig(null);
            diffFieldOb.setChildDiffMod(null);
            diffFieldOb.setChildValueMod(null);
            fieldData.add(diffFieldOb);

            //Get fields / variables 
            Field[] modifiedFields = modPropertyObject.getClass().getDeclaredFields();

            //Loop property fields of object
            for (Field modProperty : modifiedFields) {
                //Skipped fields types
                if (modProperty.isSynthetic()
                        || modProperty.isEnumConstant()) {
                    continue;
                }
                //Value of property                
                Object origvalue = null;
                Object modvalue = null;

                try {
                    //public field
                    origvalue = modProperty.get(origPropertyObject);
                    modvalue = modProperty.get(modPropertyObject);
                } catch (Exception e1) {
                    //private field with accessor
                    try {
                        String getterName = "get" + modProperty.getName().substring(0, 1).toUpperCase() + modProperty.getName().substring(1);
                        Method objgetter = modPropertyObject.getClass().getMethod(getterName);

                        origvalue = objgetter.invoke(origPropertyObject);
                        modvalue = objgetter.invoke(modPropertyObject);
                    } catch (Exception e2) {
                        //Field not accessable
                    }
                }

                //Property is complex class, i.e. property is same type of parent/current object
                //Recursive call to expand this property - if null ignore - this is for newly created objects     
                //if (modProperty.getType().getCanonicalName().trim().equals(modPropertyObject.getClass().getTypeName().trim())) {
                if ( !modProperty.getType().getCanonicalName().trim().startsWith("java.lang") && !(isCollection(modvalue)) ) {
                    diffFieldOb = new DiffField<>();

                    diffFieldOb.setName(modProperty.getName());
                    diffFieldOb.setIsParentClass(false);
                    diffFieldOb.setIsSimpleField(false);
                    diffFieldOb.setIsCreate(false);
                    diffFieldOb.setIsUpdate(true);
                    diffFieldOb.setIsDelete(false);
                    diffFieldOb.setChildValueOrig(null);
                    diffFieldOb.setChildValueMod(null);

                    if ((origvalue != null && modvalue != null)
                            && (!origvalue.equals(modvalue))) {
                        if ( hashObject != modvalue ) { 
                            //Recursive call to expand this property - if null ignore - this is for newly created objects     
                            diffFieldOb.setChildDiffMod(recursiveObjectUpdate(modPropertyObject, origvalue, modvalue));

                            fieldData.add(diffFieldOb);
                        }
                    } else if (origvalue != null && modvalue == null) {
                        //Deleted subclass i.e. person friend
                        diffFieldOb.setIsUpdate(false);
                        diffFieldOb.setIsDelete(true);
                        diffFieldOb.setChildDiffMod(null);
                        fieldData.add(diffFieldOb);
                    } else if (origvalue == null && modvalue != null) {
                        if ( hashObject != modvalue ) { 
                            //Create new subclass
                            diffFieldOb.setChildDiffMod(recursiveObjectCreate(modPropertyObject, modvalue));

                            fieldData.add(diffFieldOb);
                        }
                    }

                    continue;
                }

                if (origvalue == null && modvalue == null) {
                    continue; //Value not accessable
                }
                //Collection types
                if (isCollection(modvalue)) {

                    //Collection or Map Object
                    //Collection/Map property name text
                    diffFieldOb = new DiffField<>();

                    diffFieldOb.setName(modProperty.getName());
                    diffFieldOb.setIsParentClass(false);
                    diffFieldOb.setIsSimpleField(false);
                    diffFieldOb.setChildDiffMod(null);

                    String modValues = "{";
                    String origValues = "{";

                    //Collections                    
                    if ((origvalue != null && Collection.class.isAssignableFrom(origvalue.getClass()))
                            || (modvalue != null && Collection.class.isAssignableFrom(modvalue.getClass()))) {

                        boolean isFirst = true;

                        //Loop through original values
                        if (origvalue != null) {
                            for (Object collectionObject : (Collection) origvalue) {

                                //Add comma after first entry
                                if (!isFirst) {
                                    origValues += ",";
                                }
                                origValues += "\"" + collectionObject.toString() + "\"";
                                isFirst = false;
                            }

                        }

                        isFirst = true;
                        if (modvalue != null) {
                            //Loop through all objects of collection object
                            for (Object collectionObject : (Collection) modvalue) {

                                //Add comma after first entry
                                if (!isFirst) {
                                    modValues += ",";
                                }
                                modValues += "\"" + collectionObject.toString() + "\"";
                                isFirst = false;
                            }

                        }

                    } else if ((origvalue != null && Map.class.isAssignableFrom(origvalue.getClass()))
                            || (modvalue != null && Map.class.isAssignableFrom(modvalue.getClass()))) {
                        //Maps 
                        if (origvalue != null) {
                            Iterator mapItr = ((Map) origvalue).entrySet().iterator();
                            boolean isFirst = true;

                            //Loop through all objects of map object
                            while (mapItr.hasNext()) {
                                Map.Entry thisMapEl = (Map.Entry) mapItr.next();

                                Object objKey = thisMapEl.getKey();
                                Object objValue = thisMapEl.getValue();

                                //Add comma after first entry
                                if (!isFirst) {
                                    origValues += ",";
                                }
                                origValues += "\"" + objKey.toString() + "\"=\"" + objValue + "\"";
                                isFirst = false;
                            }

                        }

                        if (modvalue != null) {
                            Iterator mapItr = ((Map) modvalue).entrySet().iterator();
                            boolean isFirst = true;

                            //Loop through all objects of map object
                            while (mapItr.hasNext()) {
                                Map.Entry thisMapEl = (Map.Entry) mapItr.next();

                                Object objKey = thisMapEl.getKey();
                                Object objValue = thisMapEl.getValue();

                                //Add comma after first entry
                                if (!isFirst) {
                                    modValues += ",";
                                }
                                modValues += "\"" + objKey.toString() + "\"=\"" + objValue + "\"";
                                isFirst = false;
                            }

                        }

                    }
                    modValues += "}";
                    origValues += "}";

                    if (!origValues.equals("{}") && !modValues.equals("{}")) {
                        if (!origValues.equals(modValues)) {
                            //Updated
                            diffFieldOb.setIsCreate(false);
                            diffFieldOb.setIsUpdate(true);
                            diffFieldOb.setIsDelete(false);

                            diffFieldOb.setChildValueOrig(origValues);
                            diffFieldOb.setChildValueMod(modValues);
                            fieldData.add(diffFieldOb);
                        } else {
                            //Do Nothing - Equal
                        }
                    } else if (!origValues.equals("{}")) {
                        //Deleted
                        diffFieldOb.setIsCreate(false);
                        diffFieldOb.setIsUpdate(false);
                        diffFieldOb.setIsDelete(true);
                        diffFieldOb.setChildValueOrig(origValues);
                        diffFieldOb.setChildValueMod(null);
                        fieldData.add(diffFieldOb);
                    } else {
                        //Created
                        diffFieldOb.setIsCreate(true);
                        diffFieldOb.setIsUpdate(false);
                        diffFieldOb.setIsDelete(false);
                        diffFieldOb.setChildValueOrig(null);
                        diffFieldOb.setChildValueMod(modValues);
                        fieldData.add(diffFieldOb);
                    }
                } else {
                    //Simple Object - String etc.
                    diffFieldOb = new DiffField<>();

                    diffFieldOb.setName(modProperty.getName());
                    diffFieldOb.setIsParentClass(false);
                    diffFieldOb.setIsSimpleField(true);
                    diffFieldOb.setChildValueOrig(origvalue);
                    diffFieldOb.setChildValueMod(modvalue);
                    diffFieldOb.setChildDiffMod(null);

                    if (origvalue == null && modvalue != null) {
                        diffFieldOb.setIsCreate(true);
                        diffFieldOb.setIsUpdate(false);
                        diffFieldOb.setIsDelete(false);
                        fieldData.add(diffFieldOb);
                    }

                    if (origvalue != null && modvalue == null) {
                        diffFieldOb.setIsCreate(false);
                        diffFieldOb.setIsUpdate(false);
                        diffFieldOb.setIsDelete(true);
                        fieldData.add(diffFieldOb);
                    }

                    if (origvalue != null && modvalue != null) {
                        if (!origvalue.equals(modvalue)) {
                            diffFieldOb.setIsCreate(false);
                            diffFieldOb.setIsUpdate(true);
                            diffFieldOb.setIsDelete(false);
                            fieldData.add(diffFieldOb);
                        }
                    }

                }

            }

            return fieldData;
        } catch (Exception ex1) {
            throw new DiffException("Recursive Loop Update 7008 : " + ex1.getMessage());
        }
    }

    /**
     * Copy original to modified
     *
     * @param <T>
     * @param original
     * @return Copied Object
     * @throws java.lang.Exception
     */
    public static <T extends Serializable> T copyOriginal(T original) throws Exception {
        try {
            //Write original to outputstream as bytes and copy in original bytes as new object
            ByteArrayOutputStream boutstream = new ByteArrayOutputStream();
            ObjectOutputStream outstream = new ObjectOutputStream(boutstream);
            outstream.writeObject(original);

            ByteArrayInputStream binstream = new ByteArrayInputStream(boutstream.toByteArray());
            ObjectInputStream instream = new ObjectInputStream(binstream);

            //Return newObject from inputstream             
            return (T) instream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new Exception("Copy Original 3003 : " + e.getMessage());
        }
    }

    /**
     *
     * @param <T>
     * @param original
     * @param diff
     * @return
     * @throws DiffException
     */
    @Override
    public <T extends Serializable> T apply(T original, Diff<?> diff) throws DiffException {

        try {
            //Check if original object is NULL
            if (diff == null) {
                return original;
            }
            T outputObj = null;

            //If original is null, create new instance of T
            if (original == null) {
                if (diff.getFieldData() == null) {
                    return null;
                }
                if (diff.getFieldData().size() < 1) {
                    return null;
                }

                Constructor<?> cnstruct = diff.getFieldData().get(0).getClassName().getConstructor();
                outputObj = (T) cnstruct.newInstance();
            } else {
                outputObj = copyOriginal(original);
            }

            //Get modified original object as new object 
            return recsiveBuild(outputObj, (ArrayList<DiffField<?>>) diff.getFieldData());

        } catch (Exception ex1) {
            throw new DiffException("Difference Apply Exception 56001 : " + ex1.getMessage());
        }

    }

    /**
     *
     * @param <T>
     * @param originalObj
     * @param fieldDataArray
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     */
    public static <T extends Serializable> T recsiveBuild(T originalObj, ArrayList<DiffField<?>> fieldDataArray) throws NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        try {
            //Should never happen
            if (fieldDataArray == null) {
                return null;
            }
            if (originalObj == null) {
                return null;
            }

            //Loop all changes and apply to original 
            for (DiffField<?> perObj : fieldDataArray) {
                //process main class delete 
                if (perObj.isIsParentClass()) {
                    if (perObj.isIsDelete()) {
                        return null;
                    }
                    continue;
                }

                //Apply changes - create / update / delete will all just modify data in property to mod value
                if (perObj.isIsCreate() || perObj.isIsUpdate() || perObj.isIsDelete()) {
                    if (perObj.getChildDiffMod() == null) { //CHeck it is not a complex type
                        //Simple field or list
                        //TODO LIST logic

                        //Collections
                        if (Collection.class.isAssignableFrom(originalObj.getClass().getDeclaredField(perObj.getName().trim()).getType())) {
                            if (perObj.getChildValueMod() == null) {

                                //Set null as value                                                                
                                try {
                                    originalObj.getClass().getDeclaredField(perObj.getName()).set(originalObj, null);
                                } catch (Exception e1) {
                                    //private field with accessor
                                    try {
                                        String setterName = "set" + perObj.getName().substring(0, 1).toUpperCase() + perObj.getName().substring(1);

                                        Method objSetter = originalObj.getClass().getMethod(setterName,
                                                originalObj.getClass().getDeclaredField(perObj.getName().trim()).getType());

                                        objSetter.invoke(originalObj, (Object) null);

                                    } catch (Exception e2) {
                                        //Field not accessable
                                    }
                                }

                            } else {

                                //Split String to ArrayList 
                                String tmparrayString = perObj.getChildValueMod().toString().replace("{", "").replace("}", "");
                                Collection<Object> listItems;
                                listItems = Arrays.asList(tmparrayString.split("\\s*,\\s*"));

                                //Get the getter 
                                ///Remove all 
                                //AddAll new 
                                Collection collectObj = null;

                                try {
                                    //public field
                                    collectObj = (Collection) originalObj.getClass().getDeclaredField(perObj.getName()).get(originalObj);
                                } catch (Exception e1) {
                                    //private field with accessor
                                    try {
                                        String getterName = "get" + perObj.getName().substring(0, 1).toUpperCase() + perObj.getName().substring(1);
                                        Method objGetter = originalObj.getClass().getMethod(getterName);

                                        collectObj = (Collection) objGetter.invoke(originalObj);

                                        //It does not exist I have to create it 
                                        if (collectObj == null) {
                                            //Create new instance of complex class                             
                                            //Class<?> clazz = Class.forName(objGetter.getReturnType().getTypeName());
                                            //Constructor<?> ctor = clazz.getConstructor();
                                            //collectObj = (Collection) ctor.newInstance( );      
                                            //TODO : Create new instance from type name - crashing ?
                                            collectObj = new HashSet();
                                        }
                                    } catch (Exception e2) {
                                        //Field not accessable
                                        System.out.println("eex" + e2.getMessage());
                                    }
                                }

                                if (collectObj != null) {
                                    collectObj.removeAll(collectObj);
                                    collectObj.addAll(listItems);

                                    //Set collection to object - created new 
                                    try {
                                        originalObj.getClass().getDeclaredField(perObj.getName()).set(originalObj, collectObj);
                                    } catch (Exception e1) {
                                        //private field with accessor
                                        try {
                                            String setterName = "set" + perObj.getName().substring(0, 1).toUpperCase() + perObj.getName().substring(1);

                                            Method objSetter = originalObj.getClass().getMethod(setterName,
                                                    originalObj.getClass().getDeclaredField(perObj.getName().trim()).getType());

                                            objSetter.invoke(originalObj, collectObj);

                                        } catch (Exception e2) {
                                            System.out.println("ee" + e2);
                                            //Field not accessable
                                        }
                                    }
                                }
                            }

                            //Maps    
                        } else if (Map.class.isAssignableFrom(originalObj.getClass().getDeclaredField(perObj.getName().trim()).getType())) {
                            //Split String to ArrayList 
                            String tmparrayString = perObj.getChildValueMod().toString().replace("{", "").replace("}", "");
                            List<String> listItems = Arrays.asList(tmparrayString.split("\\s*,\\s*"));
                            Map<Object, Object> listMap = new HashMap<>();

                            for (String item : listItems) {
                                String key = item.split("=")[0];
                                String value = item.split("=")[1];

                                listMap.put(key, value);
                            }

                            Map collectObj = null;

                            try {
                                //public field
                                collectObj = (Map) originalObj.getClass().getDeclaredField(perObj.getName()).get(originalObj);
                            } catch (Exception e1) {
                                //private field with accessor
                                try {
                                    String getterName = "get" + perObj.getName().substring(0, 1).toUpperCase() + perObj.getName().substring(1);
                                    Method objGetter = originalObj.getClass().getMethod(getterName);

                                    collectObj = (Map) objGetter.invoke(originalObj);
                                } catch (Exception e2) {
                                    //Field not accessable
                                    System.out.println("te" + e2.getMessage());
                                }
                            }

                            if (collectObj != null) {
                                collectObj.clear();
                                collectObj.putAll(listMap);
                            }

                            //Simple Objects   
                        } else {

                            //Simple Type 
                            //apply to public field
                            try {
                                originalObj.getClass().getDeclaredField(perObj.getName()).set(originalObj, perObj.getChildValueMod());
                            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                                //private field with accessor
                                try {
                                    String setterName = "set" + perObj.getName().substring(0, 1).toUpperCase() + perObj.getName().substring(1);

                                    Method objSetter = originalObj.getClass().getMethod(setterName,
                                            originalObj.getClass().getDeclaredField(perObj.getName().trim()).getType());

                                    objSetter.invoke(originalObj, perObj.getChildValueMod());

                                } catch (Exception e2) {
                                    //Field not accessable
                                }
                            }
                        }

                    } else {
                        //Get complex type object 
                        T complexObj = null;

                        try {
                            //public field
                            complexObj = (T) originalObj.getClass().getDeclaredField(perObj.getName()).get(originalObj);
                        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                            //private field with accessor
                            try {
                                String getterName = "get" + perObj.getName().substring(0, 1).toUpperCase() + perObj.getName().substring(1);
                                Method objGetter = originalObj.getClass().getMethod(getterName);

                                complexObj = (T) objGetter.invoke(originalObj);

                                if (complexObj == null) {
                                    //Create new instance of complex class                             
                                    Constructor<?> cnstruct = objGetter.getDeclaringClass().getConstructor();
                                    complexObj = (T) cnstruct.newInstance();
                                }

                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
                                //Field not accessable
                                System.out.println("fer" + e2.getMessage());
                            }
                        }

                        //We have the property, update it
                        if (complexObj != null) {
                            //Recursive call for child property
                            T recurObj = recsiveBuild(complexObj, (ArrayList<DiffField<?>>) perObj.getChildDiffMod());

                            //apply to public field
                            try {
                                originalObj.getClass().getDeclaredField(perObj.getName()).set(originalObj, recurObj);
                            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                                //private field with accessor
                                try {
                                    String setterName = "set" + perObj.getName().substring(0, 1).toUpperCase() + perObj.getName().substring(1);
                                    Method objSetter = originalObj.getClass().getMethod(setterName,
                                            originalObj.getClass().getDeclaredField(perObj.getName().trim()).getType());

                                    objSetter.invoke(originalObj, recurObj);
                                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
                                    //Field not accessable
                                    System.out.println("gh" + e2.getMessage());
                                }
                            }

                        }

                    }
                }
            }

            return originalObj;
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
            System.out.println("te" + e.getMessage());
            throw e;
        }

    }

}
