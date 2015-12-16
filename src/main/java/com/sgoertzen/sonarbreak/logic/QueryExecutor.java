package com.sgoertzen.sonarbreak.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgoertzen.sonarbreak.model.QualityGateQuery;
import com.sgoertzen.sonarbreak.model.QualityGateResult;
import com.sgoertzen.sonarbreak.model.Sonar;
import com.sgoertzen.sonarbreak.model.SonarBreakParseException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
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

    public static QualityGateResult execute(Sonar sonar, QualityGateQuery query) throws SonarBreakParseException, IOException {
        URL queryURL = buildURL(sonar, query);
        InputStream in = queryURL.openStream();
        try {
            return parseResponse(IOUtils.toString(in));
        } finally {
            IOUtils.closeQuietly(in);
        }
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
