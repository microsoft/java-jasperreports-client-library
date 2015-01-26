JasperReports Server Client Library for DeployR
===============================================

A simple Java library for integrating DeployR capabilities into
Jaspersoft's JasperReports Server BI Suite.

This library provides an implementation of a DeployR-aware
JRQueryExecuter. This query executer generates an RDataSource.
This data source can be used to integrate R session data generated
by a DeployR R script execution into a Jasper Report. 


Links
-----

  * [Library Dependencies](#dependencies)
  * [License](#license)

Dependencies
============


Declarative JAR Dependencies: Maven Central Repository Artifacts
----------------------------------------------------------------

Artifacts for each official release (since 7.3.0) of the JasperReports
Server client library for DeployR are published to the Maven Central repository.

[ArtifactId](http://search.maven.org/#search|ga|1|a%3A%22jRQueryExecuter%22): `jRQueryExecutor`

Using build frameworks such as Maven and Gradle your client
application can simply declare a dependency on the appropriate version
of the `jRQueryExecuter` artifact to ensure all required JAR dependencies are resolved
and available at runtime.


Building the JasperReports Server Client Client Library
-------------------------------------------------------

A Gradle build script is provided to build the JasperReports Server
client library:

```
build.gradle
```

By default, the build will generate a version of the  `jRQueryExecuter-<version>.jar`
file in the `build/libs` directory.

You do not need to install Gradle before running these commands. To
build this library on a Unix based OS, run the following shell
script:

```
gradlew build
```

To build the this library on a Windows based OS, run the following
batch file:

```
gradlew.bat build
```

License
=======

Copyright (C) 2010-2015 by Revolution Analytics Inc.

This program is licensed to you under the terms of Version 2.0 of the
Apache License. This program is distributed WITHOUT
ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0) for more 
details.
