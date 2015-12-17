# sonar-break-maven-plugin

[![Build Status](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin.svg?branch=master)](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin)

A maven plugin that will fail a maven build if sonar reports errors with your project.  Works with Sonar 5.2.

## Maven 
To include in your project, update your pom.xml with the following:


    <dependencies>
        <dependency>
            <groupId>com.sgoertzen.maven</groupId>
            <artifactId>sonarbreak</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>com.sgoertzen.maven</groupId>
                <artifactId>sonarbreak</artifactId>
                <version>1.0</version>
                <configuration>
                    <sonarServer>https://sonar.yourserver.com</sonarServer>
                </configuration>
            </plugin>
        </plugins>
    </build>

You must also have the sonar plugin installed:
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>sonar-maven-plugin</artifactId>
        <version>2.7.1</version>
    </plugin>

Then run maven using the command: 
    mvn sonar:sonar sonar-break:sonar-break

Details:
sonar:sonar - This will execute the sonar task to push the code up to sonar
sonar-break:sonar-break - This will execute this plugin, which will communicate with your sonar server and will break the build if an error is found.

### Full Example
An full working pom.xml example can be seen in the integration-tests folder here: https://github.com/sgoertzen/sonar-break-maven-plugin/blob/master/integration-test/pom.xml

## Integration Tests
    cd integeration-tests
    ./run.sh

Details:
Downloads and runs a sonar server
Builds a test pom and pushes the results into sonar
Tests this plugin by fetching the sonar status