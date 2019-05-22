package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.qualitygate.*;
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
        URL url = QueryExecutor.buildUrl(sonarURL, query,QueryExecutor.SONAR_FORMAT_PATH);
        assertEquals("URL", "https://sonar.test.com/api/measures/component?componentKey=some-service&metricKeys=quality_gate_details", url.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildURLNoResourceTest() throws IllegalArgumentException, MalformedURLException {
        URL sonarURL = new URL("https://sonar.test.com");
        Query query = new Query("", "1.0");
        QueryExecutor.buildUrl(sonarURL, query,QueryExecutor.SONAR_FORMAT_PATH);
    }

    @Test
    public void parseResponseSimpleTest() throws Exception {
        String input = "{\"component\":{\"id\":7560,\"measures\":[],\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"date\":\"2015-12-10T00:52:31+0000\"}}";
        System.out.println(input);
        Result result = QueryExecutor.parseResponse(input);
        assertEquals("7560", result.getId());
    }

    @Test(expected = SonarBreakException.class)
    public void parseResponseMissingMSRTest() throws Exception {
        String input = "{\"component\":{\"id\":7560,\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\"}}";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("7560", result.getId());
    }

    @Test
    public void parseResponseExtraFieldTest() throws Exception {
        String input = "{\"component\":{\"id\":7560,\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"qualifier\":\"TRK\",\"measures\":[{\"metric\":\"quality_gate_details\",\"value\":\"{\\\"level\\\":\\\"ERROR\\\",\\\"conditions\\\":[{\\\"metric\\\":\\\"coverage\\\",\\\"op\\\":\\\"LT\\\",\\\"error\\\":\\\"15\\\",\\\"actual\\\":\\\"11.3\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"critical_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"25\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"blocker_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"0\\\",\\\"level\\\":\\\"OK\\\"}]}\"}]}}";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("7560", result.getId());
    }

    @Test
    public void parseResponseConditionsTest() throws Exception {
        String input = "{\"component\":{\"id\":7560,\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"qualifier\":\"TRK\",\"measures\":[{\"metric\":\"quality_gate_details\",\"value\":\"{\\\"level\\\":\\\"ERROR\\\",\\\"conditions\\\":[{\\\"metric\\\":\\\"coverage\\\",\\\"op\\\":\\\"LT\\\",\\\"error\\\":\\\"15\\\",\\\"actual\\\":\\\"11.3\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"critical_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"25\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"blocker_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"0\\\",\\\"level\\\":\\\"OK\\\"}]}\"}]}}";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("Level does not match", ConditionStatus.ERROR, result.getStatus());

        List<Condition> conditions = result.getConditions();
        assertNotNull("Conditions should not be null", conditions);
        assertEquals("Number of conditions does not match", 3, conditions.size());

        Condition condition = conditions.get(0);
        assertEquals("Error does not match", "15", condition.getErrorLevel());
        assertEquals("Actual does not match", "11.3", condition.getActualLevel());
        assertEquals("Name does not match", "coverage", condition.getName());
        assertEquals("Status  does not match", ConditionStatus.ERROR, condition.getStatus());
    }

    @Test
    public void parseResponseAllPropertiesTest() throws Exception {
        String input = "{\"component\":{\"id\":7560,\"key\":\"com.test.service:my-service\",\"name\":\"Service Name\",\"qualifier\":\"TRK\",\"measures\":[{\"metric\":\"quality_gate_details\",\"value\":\"{\\\"level\\\":\\\"ERROR\\\",\\\"conditions\\\":[{\\\"metric\\\":\\\"coverage\\\",\\\"op\\\":\\\"LT\\\",\\\"error\\\":\\\"15\\\",\\\"actual\\\":\\\"11.3\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"critical_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"25\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"blocker_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"0\\\",\\\"level\\\":\\\"OK\\\"}]}\"}]}}";

        Result result = QueryExecutor.parseResponse(input);
        assertEquals("Id does not match", "7560", result.getId());
        assertEquals("Key does not match", "com.test.service:my-service", result.getKey());
        assertEquals("Name does not match", "Service Name", result.getName());
        //assertEquals("Version does not match", "1.2.54", result.getVersion());

        /*DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
        DateTime expectedDateTime = formatter.parseDateTime("2015-12-10T00:52:31+0000");
        assertEquals("DateTime", expectedDateTime, result.getDatetime());*/
    }

    @Test(expected = SonarBreakException.class)
    public void parseResponseNonJSONTest() throws Exception {
        String input = "<html><body>ERROR</body></html>";
        QueryExecutor.parseResponse(input);
    }
}
