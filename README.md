# Tahopen Data Integration # 

Tahopen Data Integration (ETL)

### Project Structure

* **assemblies:** 
Project distribution archive is produced under this module
* **core:** 
Core implementation
* **dbdialog:** 
Database dialog
* **ui:** 
User interface
* **engine:** 
TDI engine
* **engine-ext:** 
TDI engine extensions
* **[plugins:](plugins/README.md)** 
TDI core plugins
* **integration:** 
Integration tests

How to build
--------------

Tahopen Data Integration uses the Maven framework. 


#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 11
* This [settings.xml](https://raw.githubusercontent.com/pentaho/maven-parent-poms/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

Add repo dependency-notsource manual maven repo
```
wget https://github.com/tahopen/legacy-jars-dependency-notsource/raw/refs/heads/main/jug-lgpl-2.0.0.jar

mvn install:install-file \
  -Dfile=jug-lgpl-2.0.0.jar \
  -DgroupId=jug-lgpl \
  -DartifactId=jug-lgpl \
  -Dversion=2.0.0 \
  -Dpackaging=jar
```

This is a Maven project, and to build it use the following command:


```
$ mvn clean install
```
Optionally you can specify -Drelease to trigger obfuscation and/or uglification (as needed)

Optionally you can specify -Dmaven.test.skip=true to skip the tests (even though
you shouldn't as you know)

The build result will be a Pentaho package located in ```target```.

#### Packaging / Distributing it

Packages can be built by using the following command:
```
$ mvn clean package
```

The packaged results will be in the `target/` sub-folders of `assemblies/*`.

For example, a distribution of the Desktop Client (CE) can then be found in: `assemblies/client/target/pdi-ce-*-SNAPSHOT.zip`.

#### Running the tests

__Unit tests__

This will run all unit tests in the project (and sub-modules). To run integration tests as well, see Integration Tests below.

```
$ mvn test
```

If you want to remote debug a single Java unit test (default port is 5005):

```
$ cd core
$ mvn test -Dtest=<<YourTest>> -Dmaven.surefire.debug
```

__Integration tests__

In addition to the unit tests, there are integration tests that test cross-module operation. This will run the integration tests.

```
$ mvn verify -DrunITs
```

To run a single integration test:

```
$ mvn verify -DrunITs -Dit.test=<<YourIT>>
```

To run a single integration test in debug mode (for remote debugging in an IDE) on the default port of 5005:

```
$ mvn verify -DrunITs -Dit.test=<<YourIT>> -Dmaven.failsafe.debug
```

To skip test

```
$ mvn clean install -DskipTests
```

To get log as text file

```
$ mvn clean install test >log.txt
```


__IntelliJ__

* Don't use IntelliJ's built-in maven. Make it use the same one you use from the commandline.
  * Project Preferences -> Build, Execution, Deployment -> Build Tools -> Maven ==> Maven home directory


### Contributing

1. Submit a pull request, referencing the relevant [Jira case](https://jira.pentaho.com/secure/Dashboard.jspa)
2. Attach a Git patch file to the relevant [Jira case](https://jira.pentaho.com/secure/Dashboard.jspa)

Use of the Pentaho checkstyle format (via `mvn checkstyle:check` and reviewing the report) and developing working 
Unit Tests helps to ensure that pull requests for bugs and improvements are processed quickly.

When writing unit tests, you have at your disposal a couple of ClassRules that can be used to maintain a healthy
test environment. Use [RestorePDIEnvironment](core/src/test/java/org/pentaho/di/junit/rules/RestorePDIEnvironment.java)
and [RestorePDIEngineEnvironment](engine/src/test/java/org/pentaho/di/junit/rules/RestorePDIEngineEnvironment.java)
for core and engine tests respectively.

pex.:
```java
public class MyTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  #setUp()...
  @Test public void testSomething() { 
    assertTrue( myMethod() ); 
  }
}
```  
