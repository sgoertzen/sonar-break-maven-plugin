package com.sgoertzen.sonarbreak;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgoertzen.sonarbreak.qualitygate.Query;
import com.sgoertzen.sonarbreak.qualitygate.Result;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Execute a query against Sonar to fetch the quality gate status for a build.  This will look for a sonar status that
 * matches the current build number and that we run in the last minute.  The query will wait up to ten minutes for
 * the results to become available on the sonar server.
 */
public class QueryExecutor {

    public static final String SONAR_FORMAT_PATH = "api/resources/index?resource=%s&metrics=quality_gate_details";
    public static final int SONAR_CONNECTION_RETRIES = 10;
    public static final int SONAR_PROCESSING_RETRIES = 60;  // Number of times to wait
    public static final int SONAR_PROCESSING_WAIT_TIME = 10000;  // wait time in milliseconds

    private final URL sonarURL;
    private final int sonarLookBackSeconds;
    private final Log log;

    /**
     * Creates a new executor for running queries against sonar.
     *
     * @param sonarServer Fully qualified URL to the sonar server
     * @param sonarLookBackSeconds Amount of time to look back into sonar history for the results of this build
     * @param log Log for logging  @return Results indicating if the build passed the sonar quality gate checks
     * @throws MalformedURLException
     */
    public QueryExecutor(String sonarServer, int sonarLookBackSeconds, Log log) throws MalformedURLException {
        this.sonarURL = new URL(sonarServer);
        this.sonarLookBackSeconds = sonarLookBackSeconds;
        this.log = log;
    }

    /**
     * Execute the given query on the specified sonar server.
     * @param query The query specifying the project and version of the build
     * @throws SonarBreakException
     * @throws IOException
     */
    public Result execute(Query query) throws SonarBreakException, IOException {
        URL queryURL = buildURL(sonarURL, query);
        log.debug("Built a sonar query url of: " + queryURL.toString());

        if (!isURLAvailable(queryURL, SONAR_CONNECTION_RETRIES)){
            throw new SonarBreakException(String.format("Unable to get a valid response after %d tries", SONAR_CONNECTION_RETRIES));
        }

        return fetchSonarStatusWithRetries(queryURL, query.getVersion());
    }

    /**
     * Get the status from sonar for the currently executing build.  This waits for sonar to complete its processing
     * before returning the results.
     *
     * @param queryURL The sonar URL to get the results from
     * @param version The current project version number
     * @return Matching result object for this build
     * @throws IOException
     * @throws SonarBreakException
     */
    private Result fetchSonarStatusWithRetries(URL queryURL, String version) throws IOException, SonarBreakException {
        int attempts = 0;
        DateTime oneMinuteAgo = DateTime.now().minusSeconds(sonarLookBackSeconds);
        do {
            Result result = fetchSonarStatus(queryURL);
            if (result.getVersion().equals(version) && result.getDatetime().isAfter(oneMinuteAgo)) {
                log.debug("Found a sonar job run that matches version and in the correct time frame");
                return result;
            }
            try {
                String message = String.format("Sleeping while waiting for sonar to process job.  Target Version: %s.  " +
                        "Sonar reporting Version: %s.  Looking back until: %s  Last result time: %s", version,
                        result.getVersion(), oneMinuteAgo.toString(), result.getDatetime().toString());
                log.debug(message);
                Thread.sleep(SONAR_PROCESSING_WAIT_TIME);
            } catch (InterruptedException e) {
                // Do nothing
            }
            attempts++;
        } while (attempts < SONAR_PROCESSING_RETRIES);

        String message = String.format("Timed out while waiting for Sonar.  Waited %d seconds",
                SONAR_PROCESSING_RETRIES*SONAR_PROCESSING_WAIT_TIME);
        throw new SonarBreakException(message);
    }

    /**
     * Get the status of a build project from sonar.  This returns the current status that sonar has and does not
     * do any checking to ensure it matches the current project
     *
     * @param queryURL The sonar URL to hit to get the status
     * @return The sonar response include quality gate status
     * @throws IOException
     * @throws SonarBreakException
     */
    private static Result fetchSonarStatus(URL queryURL) throws IOException, SonarBreakException {
        InputStream in = null;
        try {
            URLConnection connection = queryURL.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            in = connection.getInputStream();

            String response = IOUtils.toString(in);
            return parseResponse(response);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
     * the 200-399 range.
     * @param url The HTTP URL to be pinged.
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request,
     * otherwise <code>false</code>.
     */
    protected boolean isURLAvailable(URL url, int retryCount) throws IOException {
        boolean serviceFound = false;
        for (int i=0; i<retryCount; i++) {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (200 <= responseCode && responseCode <= 399) {
                log.debug(String.format("Got a valid response of %d from %s", responseCode, url));
                serviceFound = true;
                break;
            }
            else {
                try {
                    log.debug("Sleeping while waiting for sonar to become available");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
        return serviceFound;
    }

    /**
     * Creates a url for the specified quality gate query
     *
     * @param sonarURL The sonar server we will be querying
     * @param query Holds details on the query we want to make
     * @return A URL object representing the query
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     */
    protected static URL buildURL(URL sonarURL, Query query) throws MalformedURLException, IllegalArgumentException {
        if (query.getResource() == null || query.getResource().length() == 0){
            throw new IllegalArgumentException("No resource specified in the Query");
        }
        String sonarPathWithResource = String.format(SONAR_FORMAT_PATH, query.getResource());
        return new URL(sonarURL, sonarPathWithResource);
    }

    /**
     * Parses the string response from sonar into POJOs.
     *
     * @param response The json response from the sonar server.
     * @return Object representing the Sonar response
     * @throws SonarBreakException Thrown if the response is not JSON or it does not contain quality gate data.
     */
    protected static Result parseResponse(String response) throws SonarBreakException {
        ObjectMapper mapper = new ObjectMapper();
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        mapper.setDateFormat(df);
        List<Result> results;
        try {
            results = mapper.readValue(response, new TypeReference<List<Result>>() {});
        } catch (IOException e) {
            throw new SonarBreakException("Unable to parse the json into a List of QualityGateResults.  Json is: " + response, e);
        }
        if (results == null || results.size() != 1){
            throw new SonarBreakException("Unable to deserialize JSON response: " + response);
        }
        return results.get(0);
    }
}
