/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perago.techtest;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Jeanpe
 */
public class DiffField<T extends Serializable> {
    /**
     * Variables
     */
    private boolean isCreate = false;
    private boolean isUpdate = false;
    private boolean isDelete = false;
    private boolean isSimpleField = false;
    private boolean isParentClass = false;
    private String name = "";
    private Class className = null;
    private Object childValueOrig = null;
    private Object childValueMod = null;
    private ArrayList<DiffField<T>> childDiffMod = null;

    /**
     * @return the isCreate
     */
    public boolean isIsCreate() {
        return isCreate;
    }

    /**
     * @param isCreate the isCreate to set
     */
    public void setIsCreate(boolean isCreate) {
        this.isCreate = isCreate;
    }

    /**
     * @return the isUpdate
     */
    public boolean isIsUpdate() {
        return isUpdate;
    }

    /**
     * @param isUpdate the isUpdate to set
     */
    public void setIsUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    /**
     * @return the isDelete
     */
    public boolean isIsDelete() {
        return isDelete;
    }

    /**
     * @param isDelete the isDelete to set
     */
    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * @return the isSimpleField
     */
    public boolean isIsSimpleField() {
        return isSimpleField;
    }

    /**
     * @param isSimpleField the isSimpleField to set
     */
    public void setIsSimpleField(boolean isSimpleField) {
        this.isSimpleField = isSimpleField;
    }

    /**
     * @return the isParentClass
     */
    public boolean isIsParentClass() {
        return isParentClass;
    }

    /**
     * @param isParentClass the isParentClass to set
     */
    public void setIsParentClass(boolean isParentClass) {
        this.isParentClass = isParentClass;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the childValueOrig
     */
    public Object getChildValueOrig() {
        return childValueOrig;
    }

    /**
     * @param childValueOrig the childValueOrig to set
     */
    public void setChildValueOrig(Object childValueOrig) {
        this.childValueOrig = childValueOrig;
    }

    /**
     * @return the childValueMod
     */
    public Object getChildValueMod() {
        return childValueMod;
    }

    /**
     * @param childValueMod the childValueMod to set
     */
    public void setChildValueMod(Object childValueMod) {
        this.childValueMod = childValueMod;
    }

    /**
     * @return the childDiffMod
     */
    public ArrayList<DiffField<T>> getChildDiffMod() {
        return childDiffMod;
    }

    /**
     * @param childDiffMod the childDiffMod to set
     */
    public void setChildDiffMod(ArrayList<DiffField<T>> childDiffMod) {
        this.childDiffMod = childDiffMod;
    }

    /**
     * @return the className
     */
    public Class getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(Class className) {
        this.className = className;
    }

   
    
}
