package com.sgoertzen.sonarbreak.qualitygate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A quality gate condition as reported by Sonar
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Condition {

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
