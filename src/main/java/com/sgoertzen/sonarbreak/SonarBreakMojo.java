package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.qualitygate.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
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

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;
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
    @Parameter(defaultValue = "${mojoExecution}", required = true, readonly = true)
    private MojoExecution mojoExecution;


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

        if (shouldDelayExecution()) {
            //TO work with multimodule projects as well
            getLog().info("Delaying SonarQube break to the end of multi-module project");
            return;
        }

        if (StringUtils.isEmpty(sonarKey)) {

            sonarKey = String.format("%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId());
        }
        if (!StringUtils.isEmpty(sonarBranch)){

            sonarKey = String.format("%s:%s", sonarKey, sonarBranch);
        }
        final String version = mavenProject.getVersion();
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
        return session.getTopLevelProject();
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

    private boolean shouldDelayExecution() {
        return !isDetachedGoal() && !isLastProjectInReactor();
    }

    private boolean isDetachedGoal() {
        return "default-cli".equals(mojoExecution.getExecutionId());
    }

    private boolean isLastProjectInReactor() {
        List<MavenProject> sortedProjects = session.getProjectDependencyGraph().getSortedProjects();

        MavenProject lastProject = sortedProjects.isEmpty()
                ? session.getCurrentProject()
                : sortedProjects.get( sortedProjects.size() - 1 );

        return session.getCurrentProject().equals( lastProject );
    }
}
