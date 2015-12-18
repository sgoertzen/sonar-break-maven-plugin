package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.qualitygate.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QueryExecutorTest {

    @Test
    public void buildURLTest() throws MalformedURLException {
        URL sonarURL = new URL("https://sonar.test.com");
        Query query = new Query("some-service", "1.0");
        URL url = QueryExecutor.buildURL(sonarURL, query);
        assertEquals("URL", "https://sonar.test.com/api/resources/index?resource=some-service&metrics=quality_gate_details", url.toString());
    }

    @Test (expected = IllegalArgumentException.class)
    public void buildURLNoResourceTest() throws IllegalArgumentException, MalformedURLException {
        URL sonarURL = new URL("https://sonar.test.com");
        Query query = new Query("", "1.0");
        QueryExecutor.buildURL(sonarURL, query);
    }

    @Test
    public void parseResponseSimpleTest() throws Exception {
        String input = "[{\"id\":7560,\"msr\":\"\",\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"date\":\"2015-12-10T00:52:31+0000\"}]";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("7560", result.getId());
    }

    @Test(expected = SonarBreakException.class)
    public void parseResponseMissingMSRTest() throws Exception {
        String input = "[{\"id\":7560,\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"date\":\"2015-12-10T00:52:31+0000\"}]";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("7560", result.getId());
    }

    @Test(expected = SonarBreakException.class)
    public void parseResponseMissingDateTest() throws Exception {
        String input = "[{\"id\":7560,\"msr\":\"\",\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\"}]";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("7560", result.getId());
    }

    @Test
    public void parseResponseExtraFieldTest() throws Exception {
        String input = "[{\"id\":7560,\"date\":\"2015-12-10T00:52:31+0000\",\"msr\":\"\",\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"scope\":\"PRJ\"}]";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("7560", result.getId());
    }

    @Test
    public void parseResponseConditionsTest() throws Exception {
        String input = "[{\"id\":7560,\"date\":\"2015-12-10T00:52:31+0000\",\"msr\":[{\"key\":\"quality_gate_details\",\"data\":\"{\\\"level\\\":\\\"ERROR\\\",\\\"conditions\\\":[{\\\"metric\\\":\\\"coverage\\\",\\\"op\\\":\\\"LT\\\",\\\"warning\\\":\\\"25\\\",\\\"error\\\":\\\"15\\\",\\\"actual\\\":\\\"11.3\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"critical_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"25\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"blocker_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"0\\\",\\\"level\\\":\\\"OK\\\"}]}\"}]}]";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("Level does not match", ConditionStatus.ERROR, result.getStatus());

        List<Condition> conditions =  result.getConditions();
        assertNotNull("Conditions should not be null", conditions);
        assertEquals("Number of conditions does not match", 3, conditions.size());

        Condition condition = conditions.get(0);
        assertEquals("Warning does not match", "25", condition.getWarningLevel());
        assertEquals("Error does not match", "15", condition.getErrorLevel());
        assertEquals("Actual does not match", "11.3", condition.getActualLevel());
        assertEquals("Name does not match", "coverage", condition.getName());
        assertEquals("Status  does not match", ConditionStatus.ERROR, condition.getStatus());
    }

    @Test
    public void parseResponseAllPropertiesTest() throws Exception {
        String input = "[{\"id\":7560,\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"scope\":\"PRJ\",\"qualifier\":\"TRK\",\"date\":\"2015-12-10T00:52:31+0000\",\"creationDate\":\"2015-12-02T20:00:16+0000\",\"lname\":\"Service Name\",\"version\":\"1.2.54\",\"description\":\"Service Name\",\"msr\":[{\"key\":\"quality_gate_details\",\"data\":\"{\\\"level\\\":\\\"ERROR\\\",\\\"conditions\\\":[{\\\"metric\\\":\\\"coverage\\\",\\\"op\\\":\\\"LT\\\",\\\"warning\\\":\\\"25\\\",\\\"error\\\":\\\"15\\\",\\\"actual\\\":\\\"11.3\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"critical_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"25\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"blocker_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"0\\\",\\\"level\\\":\\\"OK\\\"}]}\"}]}]";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("Id does not match", "7560", result.getId());
        assertEquals("Key does not match", "com.test.service:my-service", result.getKey());
        assertEquals("Name does not match", "Service Name", result.getName());
        assertEquals("Version does not match", "1.2.54", result.getVersion());

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
        DateTime expectedDateTime = formatter.parseDateTime("2015-12-10T00:52:31+0000");
        assertEquals("DateTime", expectedDateTime, result.getDatetime());
    }

    @Test(expected = SonarBreakException.class)
    public void parseResponseNonJSONTest() throws Exception {
        String input = "<html><body>ERROR</body></html>";
        QueryExecutor.parseResponse(input);
    }
}