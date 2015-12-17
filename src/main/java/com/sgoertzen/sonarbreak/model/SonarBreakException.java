package com.sgoertzen.sonarbreak.model;

/**
 * Created by sgoertzen on 12/15/15.
 */
public class SonarBreakException extends Exception {
    public SonarBreakException(String s){
        super(s);
    }
    public SonarBreakException(String s, Throwable t){
        super(s, t);
    }
}
