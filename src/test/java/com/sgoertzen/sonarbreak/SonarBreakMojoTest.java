package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.qualitygate.ConditionStatus;
import com.sgoertzen.sonarbreak.qualitygate.Condition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SonarBreakMojoTest {

    @Test
    public void testBuildErrorString() throws Exception {
        List<Condition> conditions = new ArrayList<>();
        Condition condition = new Condition();
        condition.setName("house");
        condition.setStatus(ConditionStatus.WARNING);
        condition.setActualLevel("5");
        condition.setErrorLevel("3");
        conditions.add(condition);

        String errorString = SonarBreakMojo.buildErrorString(conditions);

        assertTrue("Must contain name", errorString.contains("house"));
        assertTrue("Must contain name", errorString.contains("3"));
        assertTrue("Must contain name", errorString.contains("5"));
    }
}
