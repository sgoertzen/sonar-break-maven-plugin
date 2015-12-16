# sonar-break-maven-plugin

[![Build Status](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin.svg?branch=master)](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin)

Will break a build if sonar reports errors with your project.  Works with Sonar 5.2.

To run this manually:
mvn com.sgoertzen.maven:sonarbreak:1.0:sonarBreak

## Maven 
Or to include in your project, update your pom.xml with the following:


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
                    <sonarServer>https://sonar.audiusasite.com</sonarServer>
                    <resourceName>com.akqa.audi.service:user-profile-parent</resourceName>
                </configuration>
            </plugin>
        </plugins>
    </build>


## Integration Tests
    cd integeration-tests
    ./run.sh

Details:
Downloads and runs a sonar server
Builds a test pom and pushes the results into sonar
Tests this plugin by fetching the sonar status