package com.sgoertzen.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Created by sgoertzen on 12/11/15.
 */
@Mojo( name = "sonarBreak" )
public class BreakMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException
    {
        getLog().info( "Hello, world." );
    }
}
