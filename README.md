# sonar-break-maven-plugin

[![Dependency Status](https://www.versioneye.com/user/projects/56983e26af789b0027001e5b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56983e26af789b0027001e5b) 
[![Codacy Badge](https://api.codacy.com/project/badge/grade/fa1e3196dc0549f1b45cf75c87c0227e)](https://www.codacy.com/app/sgoertzen/sonar-break-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.sgoertzen/sonar-break-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.sgoertzen/sonar-break-maven-plugin/)
[![Build Status](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin.svg?branch=master)](https://travis-ci.org/sgoertzen/sonar-break-maven-plugin)

A maven plugin that will fail a maven build if sonar reports errors with your project.  Tested with SonarQube 5.2 through 5.4.

## Maven 
To include in your project, update your pom.xml with the following:

    <dependencies>
        <dependency>
            <groupId>com.github.sgoertzen</groupId>
            <artifactId>sonarbreak</artifactId>
            <version>1.1.3</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>com.github.sgoertzen</groupId>
                <artifactId>sonarbreak</artifactId>
                <version>1.1.3</version>
                <configuration>
                    <sonarServer>https://sonar.yourserver.com</sonarServer>
                </configuration>
            </plugin>
        </plugins>
    </build>

### Optional parameters
There are two optional parameters that can be used with this plugin.  
* _sonarLookBackSeconds_: How far into the past the plugin should into sonar for the results of this build (default: 60)
* _waitForProcessingSeconds_: How long to wait for sonar to finish processing the job (default: 300)

These parameter goes into the configuration section so the build piece of your pom.xml would look like: 

    <build>
        <plugins>
            <plugin>
                <groupId>com.github.sgoertzen</groupId>
                <artifactId>sonarbreak</artifactId>
                <version>1.1.3</version>
                <configuration>
                    <sonarServer>https://sonar.yourserver.com</sonarServer>
                    <sonarLookBackSeconds>60</sonarLookBackSeconds>
                    <waitForProcessingSeconds>600</waitForProcessingSeconds>
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
* sonar:sonar - This will execute the sonar task to push the code up to sonar
* sonar-break:sonar-break - This will execute this plugin, which will communicate with your sonar server and will break the build if an error is found.

### Full Example
An full working pom.xml example can be seen in the integration-tests folder here: https://github.com/sgoertzen/sonar-break-maven-plugin/blob/master/integration-tests/basic/pom.xml

## Hosting
The plugin is hosted on [SonaType Nexus](https://oss.sonatype.org/content/groups/public/com/github/sgoertzen/sonar-break-maven-plugin) with the full history shown 
[here](https://oss.sonatype.org/#nexus-search;quick~sonar-break-maven-plugin)

## Development
### Build
You can build and run the tests 
```
mvn clean verify
```

Integration Test Details:
* Downloads and runs a sonar server
* Builds test poms and pushes the results into sonar
* Tests this plugin by fetching the sonar status

### Signing
If you run "mvn verify" or "mvn install" it will attempt to sign the output using gpg.  For testing purposes you may wish to just remove this step from the build.  To do this you just need to remove the execution tags on the "maven-gpg-plugin" plugin in the main pom file. 
