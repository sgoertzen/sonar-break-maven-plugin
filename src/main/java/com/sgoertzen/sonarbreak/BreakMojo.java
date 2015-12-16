package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.logic.QueryExecutor;
import com.sgoertzen.sonarbreak.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;

@Mojo( name = "sonarBreak" )
public class BreakMojo extends AbstractMojo {

    @Parameter(property = "sonarServer", required = true)
    protected String sonarServer;

    // TODO: Can we get the groupID and artifactId from the calling pom?
    @Parameter(property = "resourceName", required = true)
    protected String resourceName;

    public void execute() throws MojoExecutionException
    {
        // TODO: Get the artifact name

        try {
            Sonar sonar = new Sonar(sonarServer);
            QualityGateQuery query = new QualityGateQuery(resourceName);
            QualityGateResult result = QueryExecutor.execute(sonar, query);
            if (result.getStatus() == ConditionStatus.ERROR){
                // TODO: Include details from the conditions so they know what broke
                throw new MojoExecutionException("Build did not past sonar tests");
            }

        } catch (SonarBreakParseException | IOException e) {
            String errorMessage = String.format("Unable to parse the Sonar URL of %s", sonarServer);
            throw new MojoExecutionException(errorMessage, e);
        }
    }
}
