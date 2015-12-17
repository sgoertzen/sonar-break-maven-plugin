package com.sgoertzen.sonarbreak;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgoertzen.sonarbreak.model.QualityGateQuery;
import com.sgoertzen.sonarbreak.model.QualityGateResult;
import com.sgoertzen.sonarbreak.model.Sonar;
import com.sgoertzen.sonarbreak.model.SonarBreakException;
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

    public static QualityGateResult execute(Sonar sonar, QualityGateQuery query, Log log) throws SonarBreakException, IOException {
        URL queryURL = buildURL(sonar, query);
        log.debug("Built a sonar url of: " + queryURL.toString());

        if (!isURLAvailable(queryURL, SONAR_CONNECTION_RETRIES, log)){
            throw new SonarBreakException(String.format("Unable to get a valid response after %d tries", SONAR_CONNECTION_RETRIES));
        }

        return fetchSonarStatusWithRetries(queryURL, query.getVersion(), log);
    }

    /**
     * Get the status from sonar for the currently executing build.  This waits for sonar to complete its processing
     * before returning the results.
     *
     * @param queryURL The sonar URL to get the results from
     * @param version The current project version number
     * @param log Log to use for logging
     * @return Matching result object for this build
     * @throws IOException
     * @throws SonarBreakException
     */
    private static QualityGateResult fetchSonarStatusWithRetries(URL queryURL, String version, Log log) throws IOException, SonarBreakException {
        int attempts = 0;
        DateTime oneMinuteAgo = DateTime.now().minusMinutes(1);
        do {
            QualityGateResult qualityGateResult = fetchSonarStatus(queryURL);
            if (qualityGateResult.getVersion().equals(version) && qualityGateResult.getDatetime().isAfter(oneMinuteAgo)) {
                log.debug("Found a sonar job run that matches version and in the correct time frame");
                return qualityGateResult;
            }
            try {
                String message = String.format("Sleeping while waiting for sonar to process job.  Target Version: %s.  " +
                        "Sonar reporting Version: %s.  Looking back until: %s  Last result time: %s", version,
                        qualityGateResult.getVersion(), oneMinuteAgo.toString(), qualityGateResult.getDatetime().toString());
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
    private static QualityGateResult fetchSonarStatus(URL queryURL) throws IOException, SonarBreakException {
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
    protected static boolean isURLAvailable(URL url, int retryCount, Log log) throws IOException {
        boolean serviceFound = false;
        for(int i=0; i<retryCount; i++) {
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
     * @param sonar The sonar server we will be querying
     * @param query Holds details on the query we want to make
     * @return A URL object representing the query
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     */
    protected static URL buildURL(Sonar sonar, QualityGateQuery query) throws MalformedURLException, IllegalArgumentException {
        if (query.getResource() == null || query.getResource().length() == 0){
            throw new IllegalArgumentException("No resource specified in the QualityGateQuery");
        }
        String sonarPathWithResource = String.format(SONAR_FORMAT_PATH, query.getResource());
        return new URL(sonar.getSonarURL(), sonarPathWithResource);
    }

    /**
     * Parses the string response from sonar into POJOs.
     *
     * @param response The json response from the sonar server.
     * @return Object representing the Sonar response
     * @throws SonarBreakException Thrown if the response is not JSON or it does not contain quality gate data.
     */
    protected static QualityGateResult parseResponse(String response) throws SonarBreakException {
        ObjectMapper mapper = new ObjectMapper();
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        mapper.setDateFormat(df);
        List<QualityGateResult> results;
        try {
            results = mapper.readValue(response, new TypeReference<List<QualityGateResult>>() {});
        } catch (IOException e) {
            throw new SonarBreakException("Unable to parse the json into a List of QualityGateResults.  Json is: " + response, e);
        }
        if (results == null || results.size() != 1){
            throw new SonarBreakException("Unable to deserialize JSON response: " + response);
        }
        return results.get(0);
    }
}
