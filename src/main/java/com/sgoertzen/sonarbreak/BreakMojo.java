package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.logic.QueryExecutor;
import com.sgoertzen.sonarbreak.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.Map;

@Mojo( name = "sonarBreak", defaultPhase = LifecyclePhase.VERIFY )
public class BreakMojo extends AbstractMojo {

    @Parameter(property = "sonarServer", required = true)
    protected String sonarServer;

    public void execute() throws MojoExecutionException
    {
        Map pluginContext = this.getPluginContext();
        Object project = pluginContext.get("project");
        if (!MavenProject.class.isInstance(project))
        {
            throw new MojoExecutionException("Unable to get the group and artifact id of the building project");
        }
        MavenProject mavenProject = (MavenProject)project;
        String resourceName = String.format("%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId());

        try {
            Sonar sonar = new Sonar(sonarServer);
            QualityGateQuery query = new QualityGateQuery(resourceName);
            getLog().info("Fetching details on " + resourceName);
            QualityGateResult result = QueryExecutor.execute(sonar, query);
            getLog().info("Got a result of " + result.getStatus());
            if (result.getStatus() == ConditionStatus.ERROR){
                // TODO: Include details from the conditions so they know what broke
                throw new MojoExecutionException("Build did not past sonar tests");
            }

        } catch (SonarBreakParseException | IOException e) {
            String errorMessage = String.format("Error while running sonar Break.  Re-run with -e to see the full stack trace. ");
            throw new MojoExecutionException(errorMessage, e);
        }
    }
}
