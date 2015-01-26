/*
 * RQueryExecuterParams.java
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

import com.revo.deployr.client.data.RData;
import com.revo.deployr.client.params.AnonymousProjectExecutionOptions;

import java.util.List;
import java.util.Map;

/**
 * Report parameter "deployr" for a RQueryExecuter.
 *
 * These parameters specify the configuration for the R Script invocation
 * associated with a RQueryExecutor.
 */
public class RQueryExecuterParams {

    private String deployrUrl;
    private Map<String,String> rScript;
    private AnonymousProjectExecutionOptions execOptions;
    private String username;
    private String password;

    /**
     * Constructor.
     *
     * @param deployrUrl url address of DeployR
     * @param rScript map identifying filename, directory, author and optional version of valid R Script
     * @param execOptions script execute options
     */
    public RQueryExecuterParams(String deployrUrl,
			     Map<String,String> rScript,
                 AnonymousProjectExecutionOptions execOptions) {

	    this(deployrUrl, rScript, execOptions, null, null);
    }

    /**
     * Constructor.
     *
     * @param deployrUrl url address of DeployR
     * @param rScript map identifying filename, author and optional version of valid R Script
     * @param execOptions script execute options
     * @param username (optional) username to authenticate on secure R Script
     * @param password (optional) plain-text password to authenticate on secure R Script
     */
    public RQueryExecuterParams(String deployrUrl,
			     Map<String,String> rScript,
			     AnonymousProjectExecutionOptions execOptions,
			     String username,
			     String password) {

        this.deployrUrl = deployrUrl;
        this.rScript = rScript;
        this.execOptions = execOptions;
        this.username = username;
        this.password = password;
    }

    public String getDeployrUrl() { return deployrUrl; }

    public String getScriptFilename() { return rScript.get("filename"); }

    public String getScriptDirectory() { return rScript.get("directory"); }

    public String getScriptAuthor() { return rScript.get("author"); }

    public String getScriptVersion() { return rScript.get("version"); }

    public AnonymousProjectExecutionOptions getAnonymousProjectExecutionOptions() { return execOptions; }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

}
