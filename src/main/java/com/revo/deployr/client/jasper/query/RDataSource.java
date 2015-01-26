/*
 * RDataSource.java
 *
 * Copyright (C) 2010-2015 by Revolution Analytics Inc.
 *
 * This program is licensed to you under the terms of Version 2.0 of the
 * Apache License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0) for more details.
 *
 */
package com.revo.deployr.client.jasper.query;

import com.revo.deployr.client.RProjectFile;
import com.revo.deployr.client.data.*;

import com.revo.deployr.client.RScriptExecution;

import net.sf.jasperreports.engine.JRException; 
import net.sf.jasperreports.engine.JRField; 
import net.sf.jasperreports.engine.JRDataSource;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DeployR Data Source.
 */
public class RDataSource implements JRDataSource {

    private final static Log log = LogFactory.getLog(RDataSource.class);

    private RScriptExecution sExecution;
    private boolean available = true;
 
    public RDataSource(RScriptExecution sExecution) {
        this.sExecution = sExecution;
    }
 
    public Object getFieldValue(JRField field) throws JRException { 

        try {

            Class fieldClass = field.getValueClass();
            String description = field.getDescription();

            List<RData> workspaceObjects = sExecution.about().workspaceObjects;
            Map<String,RData> rDataMap = new HashMap<String,RData>();
            for(RData nextObj : workspaceObjects) {
                rDataMap.put(nextObj.getName(), nextObj);
            }
            Object extracted = extractFieldValue(description, fieldClass, rDataMap);
            return extracted;

        } catch(JRException jrex) {
            throw jrex;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new JRException("RDataSource: getFieldValue runtime exception=" + ex.getMessage());
        }
    }

    private Object extractFieldValue(String description, Class fieldClass,
							Map<String, RData> objectsMap)
					throws JRException {

        String[] tokens = description.split(":");

        if(tokens.length < 2) {
            throw new JRException("RDataSource: getFieldValue, invalid field description=" + description);
        }

        String fieldType = tokens[0];
        String identifier = tokens[1];

        // 1. Either extracting a File URL.
        if(fieldType.equalsIgnoreCase("file")) {
            String fileUrl = null;
            try {
                for(RProjectFile file : sExecution.about().artifacts) {
                    if(file.about().filename.equals(identifier)) {
                        fileUrl = file.download().toString();
                        break;
                    }
                }
            } catch(Exception fex) {
                log.debug("extractFieldValue: error extracting file=" + identifier + ", ex=" + fex);
            }
            return fileUrl;
        } else {
            // 2. Or extract a value from a RData.
            if(fieldType.equalsIgnoreCase("string")) {
                return extractPrimitiveValue(identifier, fieldClass, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("double")) {
                return extractPrimitiveValue(identifier, fieldClass, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("boolean")) {
                return extractPrimitiveValue(identifier, fieldClass, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("date")) {
                return extractPrimitiveValue(identifier, fieldClass, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("vector")) {
                int index = -1;
                if(tokens.length > 2) index = Integer.parseInt(tokens[2]);
                return extractVectorValue(identifier, fieldClass, index, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("matrix")) {
                int row = -1;
                if(tokens.length > 2) row = Integer.parseInt(tokens[2]);
                int index = -1;
                if(tokens.length >  3) index = Integer.parseInt(tokens[3]);
                return extractMatrixValue(identifier, fieldClass, row, index, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("list")) {
                return extractListValue(identifier, fieldClass, tokens, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("dataframe")) {
                return extractDataFrameValue(identifier, fieldClass, tokens, objectsMap);
            } else
            if(fieldType.equalsIgnoreCase("factor")) {
                int index = -1;
                if(tokens.length > 2) index = Integer.parseInt(tokens[2]);
                    return extractFactorValue(identifier, fieldClass, index, objectsMap);
            } else {
                throw new JRException("RDataSource: getFieldValue unsupported=" + description);
            }
        }
    }
 
    public boolean next() { 
        boolean hasNext = available;
        if(hasNext) available = false;
        return hasNext;
    }
 
    public void moveFirst() { 
    }

    private Object extractPrimitiveValue(String identifier,
                                        Class fieldClass,
                                        Map<String, RData> objectsMap)
                                        throws JRException{

        Object extracted = null;

        try {

            if(fieldClass.equals(String.class)) {
                RString rObject = (RString) objectsMap.get(identifier);
                extracted = rObject.getValue();
            } else
            if(fieldClass.equals(Double.class)) {
                RNumeric rObject = (RNumeric) objectsMap.get(identifier);
                extracted = rObject.getValue();
            } else
            if(fieldClass.equals(Boolean.class)) {
                RBoolean rObject = (RBoolean) objectsMap.get(identifier);
                extracted = rObject.getValue();
            } else
            if(fieldClass.equals(Date.class)) {
                RDate rObject = (RDate) objectsMap.get(identifier);
                extracted = rObject.getValue();
            }

        } catch(Exception ex) {
            throw new JRException("RDataSource: getFieldValue primitive error on " + fieldClass + ":" + identifier + " ex=" + ex.getMessage());
        }

        return extracted;
    }

    private Object extractVectorValue(String identifier,
                                        Class fieldClass,
                                        int index,
                                        Map<String, RData> objectsMap)
                                        throws JRException {

        Object extracted = null;

        try {

            if(fieldClass.equals(List.class)) {

                RData rData = objectsMap.get(identifier);

                if(rData instanceof RStringVector) {
                extracted = ((RStringVector)rData).getValue();
                } else
                if(rData instanceof RNumericVector) {
                extracted = ((RNumericVector)rData).getValue();
                } else
                if(rData instanceof RBooleanVector) {
                extracted = ((RBooleanVector)rData).getValue();
                }

            } else
            if(fieldClass.equals(String.class)) {

                RStringVector rObject = (RStringVector) objectsMap.get(identifier);
                extracted = rObject.getValue().get(index);

            } else

            if(fieldClass.equals(Double.class)) {

                RNumericVector rObject = (RNumericVector) objectsMap.get(identifier);
                extracted = rObject.getValue().get(index);

            } else

            if(fieldClass.equals(Boolean.class)) {

                RBooleanVector rObject = (RBooleanVector) objectsMap.get(identifier);
                extracted = rObject.getValue().get(index);

            }

        } catch(Exception ex) {
            ex.printStackTrace();
            throw new JRException("RDataSource: getFieldValue vector error on " + fieldClass + ":" + identifier + ":" + index + " ex=" + ex.getMessage());
        }

        return extracted; 
    }

    private Object extractMatrixValue(String identifier,
                                        Class fieldClass,
                                        int row, int index,
                                        Map<String, RData> objectsMap)
                                        throws JRException {

        Object extracted = null;

        try {

            if(fieldClass.equals(List.class)) {

                RData rData = objectsMap.get(identifier);

                if(rData instanceof RStringMatrix) {
                RStringMatrix rObject = (RStringMatrix) rData;
                    if(row < 0 && index < 0) {
                    extracted = rObject.getValue();
                    } else
                    if(index < 0)
                    extracted = rObject.getValue().get(row);
                } else
                if(rData instanceof RNumericMatrix) {
                RNumericMatrix rObject = (RNumericMatrix) rData;
                    if(row < 0 && index < 0) {
                    extracted = rObject.getValue();
                    } else
                    if(index < 0)
                    extracted = rObject.getValue().get(row);
                } else
                if(rData instanceof RBooleanMatrix) {
                RBooleanMatrix rObject = (RBooleanMatrix) rData;
                    if(row < 0 && index < 0)
                    extracted = rObject.getValue();
                    else
                    if(index < 0)
                    extracted = rObject.getValue().get(row);
                }
        
            } else
            if(fieldClass.equals(String.class)) {
        
                RStringMatrix rObject = (RStringMatrix) objectsMap.get(identifier);
                extracted = rObject.getValue().get(row).get(index);
        
            } else
        
            if(fieldClass.equals(Double.class)) {
        
                RNumericMatrix rObject = (RNumericMatrix) objectsMap.get(identifier);
                extracted = rObject.getValue().get(row).get(index);
        
            } else
        
            if(fieldClass.equals(Boolean.class)) {
        
                RBooleanMatrix rObject = (RBooleanMatrix) objectsMap.get(identifier);
                extracted = rObject.getValue().get(row).get(index);
            }

        } catch(Exception ex) {
            throw new JRException("RDataSource: getFieldValue matrix error on " + fieldClass + ":" + identifier + ":" + index + ":" + row + ":" + index + " ex=" + ex.getMessage());
        }

        return extracted;
    }

    private Object extractListValue(String identifier,
                                    Class fieldClass,
                                    String[] tokens,
                                    Map<String, RData> objectsMap)
                                    throws JRException {

        Object extracted = null;

        try {

            RList pList = (RList) objectsMap.get(identifier);
            List<RData> listObjects = pList.getValue();

            extracted = extractContainedValue(listObjects, fieldClass, tokens);

        } catch(Exception ex) {
            throw new JRException("RDataSource: getFieldValue list error on " + fieldClass + ":" + identifier + " ex=" + ex.getMessage());
        }

        return extracted;
    }

    private Object extractDataFrameValue(String identifier,
                                            Class fieldClass,
                                            String[] tokens,
                                            Map<String, RData> objectsMap)
                                            throws JRException {

        Object extracted = null;

        try {

            RDataFrame rDataFrame = (RDataFrame) objectsMap.get(identifier);
            List<RData> dataFrameObjects = rDataFrame.getValue();

            extracted = extractContainedValue(dataFrameObjects, fieldClass, tokens);

        } catch(Exception ex) {
            throw new JRException("RDataSource: getFieldValue dataframe error on " + fieldClass + ":" + identifier + " ex=" + ex.getMessage());
        }

        return extracted;
    }

    private Object extractContainedValue(List<RData> containedObjects,
                                            Class fieldClass, String[] tokens)
                                            throws JRException {

        Iterator containedIter = containedObjects.iterator();
        Map<String, RData> innerObjectsMap =
            new HashMap<String, RData>(containedObjects.size());

        while(containedIter.hasNext()) {
            RData innerRData = (RData) containedIter.next();
            innerObjectsMap.put(innerRData.getName(), innerRData);
        }

        StringBuffer buf = new StringBuffer();
        for(int i = 2; i< tokens.length; i++)
            buf.append(tokens[i]).append(":");
        String innerDescription = buf.toString();

        return extractFieldValue(innerDescription, fieldClass, innerObjectsMap);

    }

    private Object extractFactorValue(String identifier,
                                        Class fieldClass,
                                        int index,
                                        Map<String, RData> objectsMap)
                                        throws JRException {

        Object extracted = null;

        try {

            RFactor rObject = (RFactor) objectsMap.get(identifier);
            if(index < 0) {
                extracted = rObject.getValue();
            } else {
                extracted = rObject.getValue().get(index);
            }

        } catch(Exception ex) {
            throw new JRException("RDataSource: getFieldValue factor error on " + fieldClass + ":" + identifier + ":" + index + " ex=" + ex.getMessage());
        }
        
        return extracted;
    }

}

