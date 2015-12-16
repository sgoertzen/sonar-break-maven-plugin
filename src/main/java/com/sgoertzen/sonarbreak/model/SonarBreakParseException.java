package com.sgoertzen.sonarbreak.model;

/**
 * Created by sgoertzen on 12/15/15.
 */
public class SonarBreakParseException extends Exception {
    public SonarBreakParseException(String s){
        super(s);
    }
    public SonarBreakParseException(String s, Throwable t){
        super(s, t);
    }
}
