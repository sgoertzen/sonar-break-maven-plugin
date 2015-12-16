package com.sgoertzen.sonarbreak.model;

import java.net.MalformedURLException;
import java.net.URL;

public class Sonar {

    private URL sonarURL;

    public Sonar(String sonarURL) throws MalformedURLException {
        this(new URL(sonarURL));
    }
    public Sonar(URL sonarURL){
        setSonarURL(sonarURL);
    }

    public URL getSonarURL() {
        return sonarURL;
    }

    public void setSonarURL(URL sonarURL) {
        this.sonarURL = sonarURL;
    }

}
