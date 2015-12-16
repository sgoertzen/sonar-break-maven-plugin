package com.sgoertzen.sonarbreak.model;

/**
 * Created by sgoertzen on 12/14/15.
 */
public class QualityGateQuery {

    private String resource;

    public QualityGateQuery(String resource){
        setResource(resource);
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
