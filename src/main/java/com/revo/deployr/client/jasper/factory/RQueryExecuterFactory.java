/*
 * RQueryExecuterFactory.java
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
package com.revo.deployr.client.jasper.factory;

import com.revo.deployr.client.jasper.RQueryExecuter;
import com.revo.deployr.client.jasper.query.RQueryExecuterParams;

import net.sf.jasperreports.engine.JRDataset; 
import net.sf.jasperreports.engine.JRException; 
import net.sf.jasperreports.engine.query.JRQueryExecuter; 
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;
import net.sf.jasperreports.engine.fill.JRFillParameter;

import java.net.URL;
import java.io.InputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory to register new "deployrScript" Query Executer query language.
 */
public class RQueryExecuterFactory implements JRQueryExecuterFactory {

    private final static Log log = LogFactory.getLog(RQueryExecuterFactory.class);

    public Object[] getBuiltinParameters() { 
	    return new Object[]{}; 
    }

    public JRQueryExecuter createQueryExecuter(JRDataset jrd, Map map)
							throws JRException {

        log.debug("RQueryExecuterFactory createQueryExecuter jrd=" + jrd);
        log.debug("RQueryExecuterFactory createQueryExecuter map=" + map);

        String query = null;
        Map reportParamsMap = null;

        try {

            // 1. Extract query string.
            query = jrd.getQuery().getText();

            // 2. Extract the Report Parameters Map.
            JRFillParameter reportParams = (JRFillParameter) map.get("REPORT_PARAMETERS_MAP");
            reportParamsMap = (Map) reportParams.getValue();

        } catch (Exception ex) { 
	        log.debug("RQueryExecuterFactory: createQueryExecuter ex=" + ex);
            throw new JRException(ex.getMessage()); 
        }
        return new RQueryExecuter(query, reportParamsMap);
    }
 
    public boolean supportsQueryParameterType(String string) { 
        return true; 
    }

} 

