package com.sgoertzen.sonarbreak;

import com.sgoertzen.sonarbreak.model.ConditionStatus;
import com.sgoertzen.sonarbreak.model.QualityGateCondition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BreakMojoTest {

    @Test
    public void testBuildErrorString() throws Exception {
        List<QualityGateCondition> conditions = new ArrayList<>();
        QualityGateCondition condition = new QualityGateCondition();
        condition.setName("house");
        condition.setStatus(ConditionStatus.WARNING);
        condition.setActualLevel("5");
        condition.setWarningLevel("10");
        condition.setErrorLevel("3");
        conditions.add(condition);

        String errorString = BreakMojo.buildErrorString(conditions);

        assertTrue("Must contain name", errorString.contains("house"));
        assertEquals("house has a status of WARNING and a value of 5 (Warning at: 10, error at 3).", errorString);
    }
}
