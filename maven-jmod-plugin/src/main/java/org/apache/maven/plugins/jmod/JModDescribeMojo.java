package org.apache.maven.plugins.jmod;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * This goal is to support the usage of <code>jmod describe</code>.
 * 
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 */
@Mojo( name = "describe", requiresDependencyResolution = ResolutionScope.NONE, defaultPhase = LifecyclePhase.NONE )
public class JModDescribeMojo
    extends AbstractJModMojo
{

    @Parameter( defaultValue = "${project.build.directory}", required = true, readonly = true )
    private File outputDirectory;

    /**
     * The moduleName. The default is to use the <code>artifactId</code>.
     */
    @Parameter( defaultValue = "${project.artifactId}", required = true )
    private String moduleName;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        String jModExecutable;
        try
        {
            jModExecutable = getJModExecutable();
        }
        catch ( IOException e )
        {
            throw new MojoFailureException( "Unable to find jmod command: " + e.getMessage(), e );
        }

        getLog().info( "Toolchain in maven-jmod-plugin: jmod [ " + jModExecutable + " ]" );

        //TODO: Need to think about if we really require to have the jmod
        // file being located in the target directory?
        File modsFolder = new File( outputDirectory, "jmods" );
        File resultingJModFile = new File( modsFolder, moduleName + ".jmod" );

        // create the jmods folder...
        modsFolder.mkdirs();

        Commandline cmd;
        try
        {
            cmd = createJModDescribeCommandLine( resultingJModFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }
        cmd.setExecutable( jModExecutable );

        getLog().info( "The following information is contained in the module file "
            + resultingJModFile.getAbsolutePath() );
        executeCommand( cmd, outputDirectory );

    }

    private Commandline createJModDescribeCommandLine( File resultingJModFile )
        throws IOException
    {
        File file = new File( outputDirectory, "jmodDescribeArgs" );
        if ( !getLog().isDebugEnabled() )
        {
            file.deleteOnExit();
        }
        file.getParentFile().mkdirs();
        file.createNewFile();

        PrintStream argsFile = new PrintStream( file );

        argsFile.println( "describe" );

        argsFile.println( resultingJModFile.getAbsolutePath() );
        argsFile.close();

        Commandline cmd = new Commandline();
        cmd.createArg().setValue( '@' + file.getAbsolutePath() );

        return cmd;
    }

}
