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
package org.jvending.masa.plugin.aapt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

/**
 * @goal package
 * @phase package
 * @description
 */
public final class AaptPackagerMojo
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

    /**
     * @parameter default-value="${project.build.directory}/processed-resources"
     */
    public File resourceDirectory;

    /**
     * @parameter default-value="${project.build.directory}/processed-sources"
     */
    public File sourceDirectory;

    /**
     * @parameter default-value="assets"
     */
    public File assetsDirectory;

    /**
     * @parameter
     */
    public File manifestFile;

    /**
     * @parameter
     */
    public String renameManifestPackage;
    
    /**
     * @parameter
     */
    public String renameInstrumentatonTargetPackage;    

    /**
     * @parameter
     */
    public boolean includeVersionCodeInApkFile;

    /**
     * @parameter
     */
    public String versionName;

    /**
     * @parameter
     */
    public String versionCode;

    /**
     * @parameter default-value="true"
     */
    public boolean autoAddOverlay;

    /**
     * @parameter
     */
    public String configs;
    
    /**
     * @parameter
     */
    public String preferredConfigurations;
 
    /**
     * @parameter default-value="false"
     */
    public boolean debugMode;
    

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( getLog() );

        if ( manifestFile == null )
        {
            manifestFile = new File( project.getBasedir(), "AndroidManifest.xml" );
        }

        if ( !manifestFile.exists() )
        {
            throw new MojoExecutionException( "Android manifest file not found: File = "
                + manifestFile.getAbsolutePath() );
        }

        File androidJar = MasaUtil.getAndroidJarFile( project );
        if ( !androidJar.exists() )
        {
            throw new MojoExecutionException( "Android jar file not found: File = " + androidJar.getAbsolutePath() );
        }

        File outputFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".ap_" );

        List<String> commands = new ArrayList<String>();
        commands.add( "package" );

        if ( autoAddOverlay )
        {
            commands.add( "--auto-add-overlay" );
        }

        commands.add( "-f" );
        commands.add( "-M" );
        commands.add( manifestFile.getAbsolutePath() );

        commands.add( "-S" );
        if ( resourceDirectory.exists() )
        {
            commands.add( resourceDirectory.getAbsolutePath() );
        }
        else
        {
            commands.add( new File( project.getBasedir(), "res" ).getAbsolutePath() );
        }

        for ( Resource res : (List<Resource>) project.getResources() )
        {
            commands.add( "-S" );
            commands.add( res.getDirectory() );
        }

        if ( assetsDirectory.exists() )
        {
            commands.add( "-A" );
            commands.add( assetsDirectory.getAbsolutePath() );
        }
        commands.add( "-I" );
        commands.add( androidJar.getAbsolutePath() );
        commands.add( "-F" );
        commands.add( outputFile.getAbsolutePath() );

        //    --rename-manifest-package
        if ( renameManifestPackage != null )
        {
            commands.add( "--rename-manifest-package" );
            commands.add( renameManifestPackage );
        }

        if ( renameInstrumentatonTargetPackage != null )
        {
            commands.add( "--rename-instrumentation-target-package" );
            commands.add( renameInstrumentatonTargetPackage );
        }
        
        if ( versionName != null )
        {
            commands.add( "--version-name" );
            commands.add( versionName );
        }

        if ( versionCode != null )
        {
            commands.add( "--version-code" );
            commands.add( versionCode.replace( "-SNAPSHOT", "" ) );
        }

        if ( configs != null )
        {
            commands.add( "-c" );
            commands.add( configs );
        }
  
        if ( preferredConfigurations != null )
        {
            commands.add( "--preferred-configurations" );
            commands.add( preferredConfigurations );
        }
        
        if (debugMode) {
            commands.add( "--debug-mode" );
        }

        String aaptCommand = MasaUtil.getToolnameWithPath( session, project, "aapt" );
        getLog().info( aaptCommand + " " + commands.toString() );
        try
        {
            executor.executeCommand( aaptCommand, commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }
}
