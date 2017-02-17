package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.qualitygate.*;
import org.apache.commons.lang3.StringUtils;
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
@Mojo(name = "sonar-break", aggregator = true)
public class SonarBreakMojo extends AbstractMojo {

    private static final String CONDITION_FORMAT = "%s: %s level at %s (expected level of %s).";
    @Parameter(property = "sonarServer", required = true)
    protected String sonarServer;
    @Parameter(property = "sonarLookBackSeconds", defaultValue = "60")
    protected String sonarLookBackSeconds;
    @Parameter(property = "waitForProcessingSeconds", defaultValue = "300")
    protected String waitForProcessingSeconds;
    @Parameter(property = "sonar.projectKey", defaultValue = "")
    protected String sonarKey;
    @Parameter(property = "sonar.branch", defaultValue = "")
    protected String sonarBranch;

    protected static String buildErrorString(List<Condition> conditions) {
        StringBuilder builder = new StringBuilder();
        for (Condition condition : conditions) {
            builder.append("\n");
            String statusLine = String.format(CONDITION_FORMAT, condition.getStatus(), condition.getName(), condition.getActualLevel(), condition.getErrorLevel());
            builder.append(statusLine);
        }
        return builder.toString();
    }

    @Override
    public void execute() throws MojoExecutionException {
        MavenProject mavenProject = getMavenProject();
        String version = mavenProject.getVersion();
        if (StringUtils.isEmpty(sonarKey)) {

            sonarKey = String.format("%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId());
        }
        if (!StringUtils.isEmpty(sonarBranch)){

            sonarKey = String.format("%s:%s", sonarKey, sonarBranch);
        }

        getLog().info("Querying sonar for analysis on " + sonarKey + ", version: " + version);

        try {
            Query query = new Query(sonarKey, version);
            final int sonarLookBackSecondsParsed = parseParam(sonarLookBackSeconds, "sonarLookBackSeconds");
            final int waitForProcessingSecondsParsed = parseParam(waitForProcessingSeconds, "waitForProcessingSeconds");
            QueryExecutor executor = new QueryExecutor(sonarServer, sonarLookBackSecondsParsed, waitForProcessingSecondsParsed, getLog());
            Result result = executor.execute(query);
            processResult(result);

        } catch (SonarBreakException | IOException e) {
            throw new MojoExecutionException("Error while running sonar break", e);
        }
    }

    private int parseParam(final String value, final String name) throws MojoExecutionException {
        try {
            getLog().debug(String.format("Parameter %s set to value of %s", name, value));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String message = String.format("Error while parsing the %s.  The value of %s is not an integer.", name, value);
            throw new MojoExecutionException(message, e);
        }
    }

    private MavenProject getMavenProject() throws MojoExecutionException {
        Map pluginContext = this.getPluginContext();
        Object project = pluginContext.get("project");
        if (!MavenProject.class.isInstance(project)) {
            throw new MojoExecutionException("Unable to get the group and artifact id of the building project.  Maven did not pass the expected information into the plugin.");
        }
        return (MavenProject) project;
    }

    private void processResult(Result result) throws MojoExecutionException {
        getLog().info("Got a result of " + result.getStatus());

        final String errorString = buildErrorString(result.getConditions());
        switch (result.getStatus()) {
            case ERROR:
                getLog().error(errorString);
                throw new MojoExecutionException("Build did not pass sonar tests.  " + errorString);
            case WARNING:
                getLog().info("Build passed but warnings encountered.  " + errorString);
                break;
            case OK:
                getLog().info("Successfully passed Sonar checks");
                break;
            default:
                throw new MojoExecutionException("Unknown result state encountered: " + result.getStatus());
        }
    }
}
