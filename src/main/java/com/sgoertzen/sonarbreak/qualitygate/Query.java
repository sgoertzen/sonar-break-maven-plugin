package com.sgoertzen.sonarbreak.qualitygate;

/**
 * A query to check the quality gate status for a resource in sonar
 */
public class Query {

    private String resource;
    private String version;

    public Query(String resource, String version){
        setResource(resource);
        setVersion(version);
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
