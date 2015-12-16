package com.sgoertzen.sonarbreak.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sgoertzen on 12/14/15.
 */
public enum ConditionStatus {
    OK,
    WARNING,
    ERROR;


    private static Map<String, ConditionStatus> namesMap = new HashMap<String, ConditionStatus>(3);

    static {
        namesMap.put("OK", OK);
        namesMap.put("WARNING", WARNING);
        namesMap.put("ERROR", ERROR);
    }

    @JsonCreator
    public static ConditionStatus forValue(String value) {
        return namesMap.get(value);
    }

}
