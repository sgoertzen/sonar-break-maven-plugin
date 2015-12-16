package com.sgoertzen.sonarbreak.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sgoertzen.sonarbreak.logic.QualityGateResultDeserializer;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sgoertzen on 12/14/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = QualityGateResultDeserializer.class)
public class QualityGateResult {
    private String id;
    private String key;
    private String name;
    private DateTime datetime;
    private String version;
    private List<QualityGateCondition> conditions;
    private ConditionStatus status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(DateTime datetime) {
        this.datetime = datetime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<QualityGateCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<QualityGateCondition> conditions) {
        this.conditions = conditions;
    }

    public ConditionStatus getStatus() {
        return status;
    }

    public void setStatus(ConditionStatus status) {
        this.status = status;
    }
}
