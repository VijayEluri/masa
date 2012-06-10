package org.jvending.masa.it;

import java.io.File;
import java.util.Properties;

import org.apache.maven.it.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class InstallTest
    extends AbstractMavenIntegrationTestCase
{

    public InstallTest()
    {
        super( ALL_MAVEN_VERSIONS );
    }

    /**
     * 
     * @throws Exception
     */
    public void testitA()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/test-apk-install" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "target" );
        verifier.executeGoal( "install" );
      //  verifier.verifyErrorFreeLog();
        verifier.resetStreams();
       // verifier.assertFilePresent( "target/masa-24-1.0.apk" );
    }
 
}
