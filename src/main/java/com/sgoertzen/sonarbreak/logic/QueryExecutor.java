package com.sgoertzen.sonarbreak.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgoertzen.sonarbreak.model.QualityGateQuery;
import com.sgoertzen.sonarbreak.model.QualityGateResult;
import com.sgoertzen.sonarbreak.model.Sonar;
import com.sgoertzen.sonarbreak.model.SonarBreakParseException;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by sgoertzen on 12/14/15.
 */
public class QueryExecutor {

    public static final String SONAR_FORMAT_PATH = "api/resources/index?resource=%s&metrics=quality_gate_details";
    public static final int SONAR_CONNECTION_RETRIES = 10;
    public static final int SONAR_PROCESSING_RETRIES = 10;
    public static final int SONAR_PROCESSING_WAIT_TIME = 5000;

    public static QualityGateResult execute(Sonar sonar, QualityGateQuery query, Log log) throws SonarBreakParseException, IOException {
        URL queryURL = buildURL(sonar, query);

        if (!isURLAvailable(queryURL, SONAR_CONNECTION_RETRIES, log)){
            throw new SonarBreakParseException(String.format("Unable to get a valid response after %d tries", SONAR_CONNECTION_RETRIES));
        }

        return fetchSonarStatusWithRetries(queryURL, query.getVersion(), log);
    }

    private static QualityGateResult fetchSonarStatusWithRetries(URL queryURL, String version, Log log) throws IOException, SonarBreakParseException {
        int attempts = 0;
        DateTime now = DateTime.now().minusSeconds(10);
        do {
            QualityGateResult qualityGateResult = fetchSonarStatus(queryURL);
            // TODO: check version number for match
            if (qualityGateResult.getVersion().equals(version)) {
                //if (qualityGateResult.getDatetime().isAfter(now)){
                log.info("Found a sonar job run!");
                return qualityGateResult;
            }
            try {
                //String message = String.format("Sleeping while waiting for sonar to process job.  Start time: %s  Last result time: %s", now.toString(), qualityGateResult.getDatetime().toString());
                String message = String.format("Sleeping while waiting for sonar to process job.  Target Version: %s  Sonar reporting Version: %s", version, qualityGateResult.getVersion());
                log.info(message);
                Thread.sleep(SONAR_PROCESSING_WAIT_TIME);
            } catch (InterruptedException e) {
                // Do nothing
            }
            attempts++;
        } while (attempts < SONAR_PROCESSING_RETRIES);

        String message = String.format("Timed out while waiting for Sonar.  Waited %d seconds", SONAR_PROCESSING_RETRIES*SONAR_PROCESSING_WAIT_TIME);
        throw new SonarBreakParseException(message);
    }

    private static QualityGateResult fetchSonarStatus(URL queryURL) throws IOException, SonarBreakParseException {
        InputStream in = null;
        try {
            in = queryURL.openStream();
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
                serviceFound = true;
            }
            else {
                try {
                    log.info("Sleeping while waiting for sonar to become available");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
        return serviceFound;
    }

    protected static URL buildURL(Sonar sonar, QualityGateQuery query) throws MalformedURLException, IllegalArgumentException {
        if (query.getResource() == null || query.getResource().length() == 0){
            throw new IllegalArgumentException("No resource specified in the QualityGateQuery");
        }
        String sonarPathWithResource = String.format(SONAR_FORMAT_PATH, query.getResource());
        return new URL(sonar.getSonarURL(), sonarPathWithResource);
    }

    protected static QualityGateResult parseResponse(String response) throws SonarBreakParseException {
        ObjectMapper mapper = new ObjectMapper();
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        mapper.setDateFormat(df);
        List<QualityGateResult> results;
        try {
            results = mapper.readValue(response, new TypeReference<List<QualityGateResult>>() {});
        } catch (IOException e) {
            throw new SonarBreakParseException("Unable to parse the json into a List of QualityGateResults.  Json is: " + response, e);
        }
        if (results == null || results.size() != 1){
            throw new SonarBreakParseException("Unable to deserialize response");
        }
        return results.get(0);
    }
}
