package com.sgoertzen.sonarbreak.qualitygate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.joda.time.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = CEResponseDeserialiser.class)
public class CEResponse {
    private String componentKey;
    private DateTime analysisTime;

    public DateTime getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(DateTime analysisTime) {
        this.analysisTime = analysisTime;
    }

    public String getComponentKey() {
        return componentKey;
    }

    public void setComponentKey(String componentKey) {
        this.componentKey = componentKey;
    }

}
