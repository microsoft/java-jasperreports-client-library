/*
 * RQueryExecuter.java
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
package com.revo.deployr.client.jasper;

import com.revo.deployr.client.RScriptExecution;
import com.revo.deployr.client.jasper.query.RDataSource;

import com.revo.deployr.client.RClient;
import com.revo.deployr.client.RClientException;
import com.revo.deployr.client.RDataException;
import com.revo.deployr.client.RGridException;
import com.revo.deployr.client.RSecurityException;
import com.revo.deployr.client.data.*;
import com.revo.deployr.client.factory.RDataFactory;
import com.revo.deployr.client.factory.RClientFactory;
import com.revo.deployr.client.auth.basic.RBasicAuthentication;
import com.revo.deployr.client.jasper.query.RQueryExecuterParams;
import com.revo.deployr.client.params.ProjectPreloadOptions;
import com.revo.deployr.client.params.AnonymousProjectExecutionOptions;

import net.sf.jasperreports.engine.JRDataSource; 
import net.sf.jasperreports.engine.JREmptyDataSource; 
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Jasper Reports query executer for integrating with the
 * DeployR R Integration Server.
 * <p/>
 */
public class RQueryExecuter implements JRQueryExecuter {

    private final static Log log = LogFactory.getLog(RQueryExecuter.class);
    private static final String DEPLOYR_PARAM_PREFIX = "RRP_";

    private String query;
    private Map reportParamsMap; 
    private RQueryExecuterParams queryExecuterParams;
    private RScriptExecution sExecution;


    public RQueryExecuter(String query, Map reportParamsMap) {
        this.query = query;
        this.reportParamsMap = reportParamsMap;
    }

    public JRDataSource createDatasource() throws JRException {

        try {

            log.debug("RQueryExecuter: begins, about to buildQueryParams.");
            queryExecuterParams = buildQueryParams(query, reportParamsMap);

            if(queryExecuterParams == null) {
                log.debug("RQueryExecuter: buildQueryParams returns null, throwing exception.");
                throw new Exception("PQueryExecuter build query params returns null.");
            }

            log.debug("RQueryExecuter: about to RClientFactory.creqteClient with url=" + queryExecuterParams.getDeployrUrl());
            RClient rClient = RClientFactory.createClient(queryExecuterParams.getDeployrUrl());

            log.debug("RQueryExecuter: after RClientFactory.creqteClient.");
            if(queryExecuterParams.getUsername() != null &&
                                queryExecuterParams.getPassword() != null) {
                log.debug("RQueryExecuter: about to RClient.login with username=" + queryExecuterParams.getUsername());
                rClient.login(new RBasicAuthentication(queryExecuterParams.getUsername(),
                                                        queryExecuterParams.getPassword()));
            }

            log.debug("RQueryExecuter: about to call rClient.executeScript..."); 
            sExecution = rClient.executeScript(queryExecuterParams.getScriptFilename(),
                                                queryExecuterParams.getScriptDirectory(),
                                                queryExecuterParams.getScriptAuthor(),
                                                queryExecuterParams.getScriptVersion(),
                                                queryExecuterParams.getAnonymousProjectExecutionOptions());

           log.debug("RQueryExecuter: after call to rClient.executeScript..."); 

        } catch (RGridException pge) {
           log.debug("RQueryExecuter: RGridException=" + pge.getMessage());
            throw new JRException(pge.getMessage());
        } catch (RDataException pde) {
           log.debug("RQueryExecuter: RDataException=" + pde.getMessage());
            throw new JRException(pde.getMessage());
        } catch (RClientException pce) {
           log.debug("RQueryExecuter: RClientException=" + pce.getMessage());
            throw new JRException(pce.getMessage());
        } catch (RSecurityException pse) {
           log.debug("RQueryExecuter: RSecurityException=" + pse.getMessage());
            throw new JRException(pse.getMessage());
        } catch (Exception ex) {
            log.debug("RQueryExecuter: runtime Exception=" + ex.getMessage());
            throw new JRException("RQueryExecuter runtime exception=" + ex.getMessage());
        }

        log.debug("RQueryExecuter: about to return new RDataSource.");
        return new RDataSource(sExecution); 
    }
 
    public void close() { }
 
    public boolean cancelQuery() throws JRException { 
        return false; 
    }

    private RQueryExecuterParams buildQueryParams(String query, Map reportParams)
						throws JRException {

        RQueryExecuterParams queryExecuterParams = null;

        try {

            log.debug("buildQueryParams: begins, query=" + query + " map=" + reportParams);

            // 1. Process "deployrScript" Query String.
            InputSource inputSource = new InputSource(new StringReader(query));
            log.debug("buildQueryParams: have inputSource.");
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            log.debug("buildQueryParams: have DocumentBuilder.");
            Document document = builder.parse(inputSource);
            log.debug("buildQueryParams: have successfully parsed document.");

            Element root = document.getDocumentElement();
            String deployrUrl = root.getAttribute("url");

            NodeList script = document.getElementsByTagName("script");
            log.debug("buildQueryParams: getElementsByTagName for script found=" + script);
            Map<String,String> rScript = new HashMap<String,String>();
            if(script != null && script.getLength() == 1) {
                Element ws = (Element) script.item(0);
                log.debug("buildQueryParams: script.item(0) ws=" + ws);
                rScript.put("filename", ws.getAttribute("filename"));
                rScript.put("directory", ws.getAttribute("directory"));
                rScript.put("author", ws.getAttribute("author"));
                rScript.put("version", ws.getAttribute("version"));
            } else {
                log.debug("buildQueryParams: script.getLength=" + script.getLength());
            }

            log.debug("buildQueryParams: found deployrUrl=" + deployrUrl + " rScript=" + rScript);

            List<String> outputsList = null;
            NodeList robjects = document.getElementsByTagName("robject");
            log.debug("buildQueryParams: getElementsByTagName for robject found=" + robjects);
            if(robjects != null && robjects.getLength() > 0) {
                outputsList = new ArrayList<String>(robjects.getLength());
                for(int i=0; i< robjects.getLength(); i++) {
                    Element robject = (Element) robjects.item(i);
                    String robjectName = robject.getAttribute("name");
                    outputsList.add(robjectName);
                }
            }

            ProjectPreloadOptions preloadWorkspace = null;
            NodeList workspace = document.getElementsByTagName("workspace");
            log.debug("buildQueryParams: getElementsByTagName for workspace found=" + workspace);
            if(workspace != null && workspace.getLength() == 1) {
                Element ws = (Element) workspace.item(0);
                log.debug("buildQueryParams: workspace.item(0) ws=" + ws);
                preloadWorkspace = new ProjectPreloadOptions();
                preloadWorkspace.filename = ws.getAttribute("filename");
                preloadWorkspace.directory = ws.getAttribute("directory");
                preloadWorkspace.author = ws.getAttribute("author");
                preloadWorkspace.version = ws.getAttribute("version");
            } else {
                log.debug("buildQueryParams: workspace.getLength=" + workspace.getLength());
            }

            ProjectPreloadOptions preloadDirectory = null;
            NodeList directory = document.getElementsByTagName("directory");
            log.debug("buildQueryParams: getElementsByTagName for directory found=" + directory);
            if(directory != null && directory.getLength() == 1) {
                Element dir = (Element) directory.item(0);
                preloadDirectory = new ProjectPreloadOptions();
                preloadDirectory.filename = dir.getAttribute("filename");
                preloadDirectory.directory = dir.getAttribute("directory");
                preloadDirectory.author = dir.getAttribute("author");
                preloadDirectory.version = dir.getAttribute("version");
            }

            String username = null;
            String password = null;

            NodeList authentication = document.getElementsByTagName("authentication");
            log.debug("buildQueryParams: getElementsByTagName for authentication found=" + authentication);
            if(authentication != null && authentication.getLength() == 1) {
                Element auth = (Element) authentication.item(0);
                username = auth.getAttribute("username");
                password = auth.getAttribute("password");
            }

            // 2. Process DPP_*, DeployR Report Parameters.
            List<RData> inputsList = null;
            if(reportParams != null) {
                inputsList = new ArrayList<RData>();
                Iterator iter = reportParams.entrySet().iterator();
                while(iter.hasNext()) {
                    Map.Entry nameValue = (Map.Entry) iter.next();
                    String name = (String) nameValue.getKey();
                    if(name.startsWith(DEPLOYR_PARAM_PREFIX)) {
                        Object value = nameValue.getValue();
                        log.debug("buildQueryParams: about to buildRData for name=" + name + " value=" + value + " value.clazz=" + value.getClass());
                        RData rData = buildRData(name, value);
                        if(rData != null) {
                            inputsList.add(rData);
                        } else {
                            throw new JRException("RQueryExecuter: " + name + " unsupported data type.");
                        }
                    }
                }
            }

            AnonymousProjectExecutionOptions execOptions = new AnonymousProjectExecutionOptions();
            execOptions.rinputs = inputsList;
            execOptions.routputs = outputsList;
            execOptions.preloadWorkspace = preloadWorkspace;
            execOptions.preloadDirectory = preloadDirectory;

            queryExecuterParams = new RQueryExecuterParams(deployrUrl,
                                                            rScript,
                                                            execOptions,
                                                            username,
                                                            password);

        } catch(Exception ex) {
            log.debug("buildQueryParams: processing query string failed=" + ex.getMessage());
            throw new JRException("RQueryExecuter processing query string failed.", ex);
        }

        return queryExecuterParams;
    }
 
    private RData buildRData(String name, Object value) {

        RData rData = null;

        String shortName = name.substring(4);

        if(value instanceof String)
            rData = RDataFactory.createString(shortName, (String) value);
        else
        if(value instanceof Integer)
            rData = RDataFactory.createNumeric(shortName, ((Integer) value).doubleValue());
        else
        if(value instanceof Double)
            rData = RDataFactory.createNumeric(shortName, (Double) value);
        else
        if(value instanceof Float)
            rData = RDataFactory.createNumeric(shortName, ((Float) value).doubleValue());
        else
        if(value instanceof BigDecimal)
            rData = RDataFactory.createNumeric(shortName, ((BigDecimal) value).doubleValue());
        else
        if(value instanceof Boolean)
            rData = RDataFactory.createBoolean(shortName, (Boolean) value);
        else
        if(value instanceof Date)
            rData = RDataFactory.createDate(shortName, (Date) value);

        return rData;
    }
 
}
