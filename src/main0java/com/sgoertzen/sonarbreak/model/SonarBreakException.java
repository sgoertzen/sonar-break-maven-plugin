package com.sgoertzen.sonarbreak.model;

public class SonarBreakException extends Exception {
    public SonarBreakException(String s){
        super(s);
    }
    public SonarBreakException(String s, Throwable t){
        super(s, t);
    }
}
