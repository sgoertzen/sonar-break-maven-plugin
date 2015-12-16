# sonar-break-maven-plugin

[![Build Status](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin.svg?branch=master)](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin)

Will break a build if sonar reports errors with your project.  Works with Sonar 5.2.

To run this manually:
mvn com.sgoertzen.maven:sonarbreak:1.0:sonarBreak

Or to include in your project update your pom.xml to include:


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
