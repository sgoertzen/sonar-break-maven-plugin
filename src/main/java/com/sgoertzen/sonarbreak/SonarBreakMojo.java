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

import static com.sgoertzen.sonarbreak.model.ConditionStatus.*;

/**
 * Custom maven plugin to break a maven build if sonar rules are not met.
 */
@Mojo( name = "sonar-break", aggregator = true )
public class SonarBreakMojo extends AbstractMojo {

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
        getLog().info("Fetching details on " + resourceName + ", version: " + version);

        try {
            Sonar sonar = new Sonar(sonarServer);
            QualityGateQuery query = new QualityGateQuery(resourceName, version);
            QualityGateResult result = QueryExecutor.execute(sonar, query, getLog());
            getLog().info("Got a result of " + result.getStatus());
            switch (result.getStatus()){
                case ERROR:
                    throw new MojoExecutionException("Build did not past sonar tests.  " + buildErrorString(result.getConditions()));
                case WARNING:
                    getLog().info("Build passed but warnings encountered.  " + buildErrorString(result.getConditions()));
                    break;
                case OK:
                    getLog().info("Successfully passed Sonar checks");
                    break;
                default:
                    throw new MojoExecutionException("Unknown result state encountered: " + result.getStatus());
            }

        } catch (SonarBreakException | IOException e) {
            String errorMessage = "Error while running sonar Break.  Re-run with -e to see the full stack trace.";
            throw new MojoExecutionException(errorMessage, e);
        }
    }

    private static final String CONDITION_FORMAT = "%s: %s level at %s (must be beyond %s).";

    protected static String buildErrorString(List<QualityGateCondition> conditions) {
        StringBuilder builder = new StringBuilder();
        for(QualityGateCondition condition : conditions){
            builder.append("\n");
            String statusLine = String.format(CONDITION_FORMAT, condition.getStatus(), condition.getName(), condition.getActualLevel(), condition.getErrorLevel());
            builder.append(statusLine);
        }
        return builder.toString();
    }
}
