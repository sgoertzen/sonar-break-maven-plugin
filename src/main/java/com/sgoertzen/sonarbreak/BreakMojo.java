package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Custom maven plugin to break a maven build if sonar rules are not met.
 */
@Mojo( name = "sonar-break" )
public class BreakMojo extends AbstractMojo {

    @Parameter(property = "sonarServer", required = true)
    protected String sonarServer;

    public void execute() throws MojoExecutionException
    {
        Map pluginContext = this.getPluginContext();
        Object project = pluginContext.get("project");
        if (!MavenProject.class.isInstance(project))
        {
            throw new MojoExecutionException("Unable to get the group and artifact id of the building project.  Maven did not pass the expected information into the plugin.");
        }
        MavenProject mavenProject = (MavenProject)project;
        String version = mavenProject.getVersion();
        String resourceName = String.format("%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId());

        try {
            Sonar sonar = new Sonar(sonarServer);
            QualityGateQuery query = new QualityGateQuery(resourceName, version);
            getLog().info("Fetching details on " + resourceName + ", version: " + version);
            QualityGateResult result = QueryExecutor.execute(sonar, query, getLog());
            getLog().info("Got a result of " + result.getStatus());
            if (result.getStatus() == ConditionStatus.ERROR){
                String errorMessage = buildErrorString(result.getConditions());
                throw new MojoExecutionException("Build did not past sonar tests.  " + errorMessage);
            }

        } catch (SonarBreakException | IOException e) {
            String errorMessage = String.format("Error while running sonar Break.  Re-run with -e to see the full stack trace. ");
            throw new MojoExecutionException(errorMessage, e);
        }
    }

    private static final String CONDITION_FORMAT = "%s has a status of %s and a value of %s (Warning at: %s, error at %s).";

    protected static String buildErrorString(List<QualityGateCondition> conditions) {
        StringBuilder builder = new StringBuilder();
        for(QualityGateCondition condition : conditions){
            if (builder.length() > 0){
                builder.append("\n");
            }
            String statusLine = String.format(CONDITION_FORMAT, condition.getName(), condition.getStatus(), condition.getActualLevel(), condition.getWarningLevel(), condition.getErrorLevel());
            builder.append(statusLine);
        }
        return builder.toString();
    }
}
