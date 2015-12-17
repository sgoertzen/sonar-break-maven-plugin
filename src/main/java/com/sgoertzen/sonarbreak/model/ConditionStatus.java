package com.sgoertzen.sonarbreak.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent the status of a sonar condition
 */
public enum ConditionStatus {
    OK,
    WARNING,
    ERROR;


    private static Map<String, ConditionStatus> namesMap = new HashMap<>(3);

    static {
        namesMap.put("OK", OK);
        namesMap.put("WARN", WARNING);
        namesMap.put("ERROR", ERROR);
    }

    @JsonCreator
    public static ConditionStatus forValue(String value) {
        return namesMap.get(value);
    }

}
