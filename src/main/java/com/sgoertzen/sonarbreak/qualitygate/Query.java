package com.sgoertzen.sonarbreak.qualitygate;

/**
 * A query to check the quality gate status for a sonarKey in sonar
 */
public class Query {

    private String sonarKey;
    private String version;

    public Query(String sonarKey, String version) {
        setSonarKey(sonarKey);
        setVersion(version);
    }

    public String getSonarKey() {
        return sonarKey;
    }

    public void setSonarKey(String sonarKey) {
        this.sonarKey = sonarKey;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
