package com.sgoertzen.sonarbreak.model;

/**
 * Created by sgoertzen on 12/14/15.
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QualityGateCondition {

    @JsonProperty("metric")
    private String Name;
    @JsonProperty("warning")
    private String WarningLevel;
    @JsonProperty("error")
    private String ErrorLevel;
    @JsonProperty("actual")
    private String ActualLevel;
    @JsonProperty("level")
    private ConditionStatus status;

    public ConditionStatus getStatus() {
        return status;
    }

    public void setStatus(ConditionStatus status) {
        this.status = status;
    }

    public String getActualLevel() {
        return ActualLevel;
    }

    public void setActualLevel(String actualLevel) {
        ActualLevel = actualLevel;
    }

    public String getErrorLevel() {
        return ErrorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        ErrorLevel = errorLevel;
    }

    public String getWarningLevel() {
        return WarningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        WarningLevel = warningLevel;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

}
