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

Then run maven using the command: 
    mvn com.sgoertzen.maven:sonarbreak:1.0:sonarBreak


## Integration Tests
    cd integeration-tests
    ./run.sh

Details:
Downloads and runs a sonar server
Builds a test pom and pushes the results into sonar
Tests this plugin by fetching the sonar status