package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.qualitygate.*;
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
@Mojo( name = "sonar-break", aggregator = true )
public class SonarBreakMojo extends AbstractMojo {

    @Parameter(property = "sonarServer", required = true)
    protected String sonarServer;

    @Parameter(property = "sonarLookBackSeconds", defaultValue = "60")
    protected String sonarLookBackSeconds;

    private static final String CONDITION_FORMAT = "%s: %s level at %s (must be beyond %s).";

    public void execute() throws MojoExecutionException
    {
        MavenProject mavenProject = getMavenProject();
        String version = mavenProject.getVersion();
        String resourceName = String.format("%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId());
        getLog().info("Fetching details on " + resourceName + ", version: " + version);

        try {
            Query query = new Query(resourceName, version);
            QueryExecutor executor = new QueryExecutor(sonarServer, parseLookback(), getLog());
            Result result = executor.execute(query);
            processResult(result);

        } catch (SonarBreakException | IOException e) {
            throw new MojoExecutionException("Error while running sonar break", e);
        }
    }

    private int parseLookback() throws MojoExecutionException {
        try {
            return Integer.parseInt(sonarLookBackSeconds);
        }
        catch (NumberFormatException e){
            String message = String.format("Error while parsing the sonarLookBackSeconds.  The value of %s is not an integer.", sonarLookBackSeconds);
            throw new MojoExecutionException(message, e);
        }
    }


    private MavenProject getMavenProject() throws MojoExecutionException {
        Map pluginContext = this.getPluginContext();
        Object project = pluginContext.get("project");
        if (!MavenProject.class.isInstance(project))
        {
            throw new MojoExecutionException("Unable to get the group and artifact id of the building project.  Maven did not pass the expected information into the plugin.");
        }
        return (MavenProject)project;
    }

    private void processResult(Result result) throws MojoExecutionException {
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
    }

    protected static String buildErrorString(List<Condition> conditions) {
        StringBuilder builder = new StringBuilder();
        for (Condition condition : conditions){
            builder.append("\n");
            String statusLine = String.format(CONDITION_FORMAT, condition.getStatus(), condition.getName(), condition.getActualLevel(), condition.getErrorLevel());
            builder.append(statusLine);
        }
        return builder.toString();
    }
}
