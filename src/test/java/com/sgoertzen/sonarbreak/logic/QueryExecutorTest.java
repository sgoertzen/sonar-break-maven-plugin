package com.sgoertzen.sonarbreak.logic;

import com.sgoertzen.sonarbreak.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class QueryExecutorTest {

    @Test
    public void buildURLTest() throws MalformedURLException {
        Sonar sonar = new Sonar(new URL("https://sonar.test.com"));
        QualityGateQuery query = new QualityGateQuery("some-service");
        URL url = QueryExecutor.buildURL(sonar, query);
        Assert.assertEquals("URL", "https://sonar.test.com/api/resources/index?resource=some-service&metrics=quality_gate_details", url.toString());
    }

    @Test (expected = IllegalArgumentException.class)
    public void buildURLNoResourceTest() throws IllegalArgumentException, MalformedURLException {
        Sonar sonar = new Sonar(new URL("https://sonar.test.com"));
        QualityGateQuery query = new QualityGateQuery("");
        QueryExecutor.buildURL(sonar, query);
    }


    @Test
    public void parseResponseSimpleTest() throws Exception {
        String input = "[{\"id\":7560,\"key\":\"com.akqa.audi.service:user-profile-parent\",\"name\":\"User Profile Service - Parent\"}]";

        QualityGateResult result = QueryExecutor.parseResponse(input);
        Assert.assertEquals("7560", result.getId());
    }

    @Test
    public void parseResponseExtraFieldTest() throws Exception {
        String input = "[{\"id\":7560,\"key\":\"com.akqa.audi.service:user-profile-parent\",\"name\":\"User Profile Service - Parent\",\"scope\":\"PRJ\"}]";

        QualityGateResult result = QueryExecutor.parseResponse(input);
        Assert.assertEquals("7560", result.getId());
    }

    @Test
    public void parseResponseConditionsTest() throws Exception {
        String input = "[{\"id\":7560,\"msr\":[{\"key\":\"quality_gate_details\",\"data\":\"{\\\"level\\\":\\\"ERROR\\\",\\\"conditions\\\":[{\\\"metric\\\":\\\"coverage\\\",\\\"op\\\":\\\"LT\\\",\\\"warning\\\":\\\"25\\\",\\\"error\\\":\\\"15\\\",\\\"actual\\\":\\\"11.3\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"critical_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"25\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"blocker_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"0\\\",\\\"level\\\":\\\"OK\\\"}]}\"}]}]";

        QualityGateResult result = QueryExecutor.parseResponse(input);
        Assert.assertEquals("Level does not match", ConditionStatus.ERROR, result.getStatus());

        List<QualityGateCondition> conditions =  result.getConditions();
        Assert.assertNotNull("Conditions should not be null", conditions);
        Assert.assertEquals("Number of conditions does not match", 3, conditions.size());

        QualityGateCondition condition = conditions.get(0);
        Assert.assertEquals("Warning does not match", "25", condition.getWarningLevel());
        Assert.assertEquals("Error does not match", "15", condition.getErrorLevel());
        Assert.assertEquals("Actual does not match", "11.3", condition.getActualLevel());
        Assert.assertEquals("Name does not match", "coverage", condition.getName());
        Assert.assertEquals("Status  does not match", ConditionStatus.ERROR, condition.getStatus());
    }

    @Test
    public void parseResponseAllPropertiesTest() throws Exception {
        String input = "[{\"id\":7560,\"key\":\"com.akqa.audi.service:user-profile-parent\",\"name\":\"User Profile Service - Parent\",\"scope\":\"PRJ\",\"qualifier\":\"TRK\",\"date\":\"2015-12-10T00:52:31+0000\",\"creationDate\":\"2015-12-02T20:00:16+0000\",\"lname\":\"User Profile Service - Parent\",\"version\":\"1.2.54\",\"description\":\"User Profile Service - Parent\",\"msr\":[{\"key\":\"quality_gate_details\",\"data\":\"{\\\"level\\\":\\\"ERROR\\\",\\\"conditions\\\":[{\\\"metric\\\":\\\"coverage\\\",\\\"op\\\":\\\"LT\\\",\\\"warning\\\":\\\"25\\\",\\\"error\\\":\\\"15\\\",\\\"actual\\\":\\\"11.3\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"critical_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"25\\\",\\\"level\\\":\\\"ERROR\\\"},{\\\"metric\\\":\\\"blocker_violations\\\",\\\"op\\\":\\\"NE\\\",\\\"warning\\\":\\\"\\\",\\\"error\\\":\\\"0\\\",\\\"actual\\\":\\\"0\\\",\\\"level\\\":\\\"OK\\\"}]}\"}]}]";

        QualityGateResult result = QueryExecutor.parseResponse(input);
        Assert.assertEquals("Id does not match", "7560", result.getId());
        Assert.assertEquals("Key does not match", "com.akqa.audi.service:user-profile-parent", result.getKey());
        Assert.assertEquals("Name does not match", "User Profile Service - Parent", result.getName());
        Assert.assertEquals("Version does not match", "1.2.54", result.getVersion());

        // TODO: Uncomment these
        //Assert.assertEquals("Date", "", result.getDate());
    }

    @Test(expected = SonarBreakParseException.class)
    public void parseResponseNonJSONTest() throws Exception {
        String input = "<html><body>ERROR</body></html>";

        QualityGateResult result = QueryExecutor.parseResponse(input);
        Assert.assertEquals("7560", result.getId());
    }
}