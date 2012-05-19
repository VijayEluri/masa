/*
 * Copyright (C) 2007-2008 JVending Masa
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jvending.masa.plugin.aidl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

/**
 * @goal generate
 * @requiresProject true
 * @description
 */
public class AidlGeneratorMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * @parameter expression="${session}"
     */
    public MavenSession session;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File sourceDir = new File( project.getBuild().getSourceDirectory() );
        if ( !sourceDir.exists() )
        {
            return;
        }

        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( project.getBuild().getSourceDirectory() );

        List<String> excludeList = new ArrayList<String>();
        // target files
        excludeList.add( "target/**" );

        List<String> includeList = new ArrayList<String>();
        includeList.add( "**/*.aidl" );
        String[] includes = new String[includeList.size()];
        directoryScanner.setIncludes( ( includeList.toArray( includes ) ) );
        directoryScanner.addDefaultExcludes();

        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();
        getLog().info( "ANDROID-904-002: Found aidl files: Count = " + files.length );
        if ( files.length == 0 )
        {
            return;
        }

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        File generatedSourcesDirectory = new File( project.getBuild().getDirectory() + File.separator
            + "generated-sources" + File.separator + "aidl" );
        generatedSourcesDirectory.mkdirs();

        for ( String file : files )
        {
            List<String> commands = new ArrayList<String>();
            String androidVersion = MasaUtil.getAndroidVersion( session, project );
            if ( System.getenv().get( "ANDROID_SDK" ) != null )
            {
                commands.add( "-p" + System.getenv().get( "ANDROID_SDK" ) + "/platforms/android-" + androidVersion
                    + "/framework.aidl" );
            }
            else
            {

                commands.add( "-p" + findAidlLibraryFor( androidVersion, MasaUtil.getToolpaths( session, project ) ) );
            }
            File targetDirectory = new File( generatedSourcesDirectory, new File( file ).getParent() );
            targetDirectory.mkdirs();

            String fileName = new File( file ).getName();

            commands.add( "-I" + project.getBuild().getSourceDirectory() );
            commands.add( ( new File( project.getBuild().getSourceDirectory(), file ).getAbsolutePath() ) );
            commands.add( new File( targetDirectory, fileName.substring( 0, fileName.lastIndexOf( "." ) ) + ".java" )
                .getAbsolutePath() );
            try
            {
                executor.executeCommand( MasaUtil.getToolnameWithPath( session, project, "aidl" ), commands,
                                         project.getBasedir(), false );
            }
            catch ( ExecutionException e )
            {
                throw new MojoExecutionException( "", e );
            }
        }

        project.addCompileSourceRoot( generatedSourcesDirectory.getPath() );

    }

    private File findAidlLibraryFor( String androidVersion, List<File> tools )
    {
        String frameworkFile = "platforms/android-" + androidVersion + "/framework.aidl";

        for ( File toolPath : tools )
        {
            File checkFile = new File( toolPath.getParent(), frameworkFile );
            if ( checkFile.exists() )
            {
                return checkFile;
            }
        }
        return null;
    }
}
