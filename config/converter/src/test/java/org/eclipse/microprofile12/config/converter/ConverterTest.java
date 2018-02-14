package org.eclipse.microprofile12.config.converter;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Arjan Tijms
 */
@RunWith(Arquillian.class)
public class ConverterTest {

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = 
            create(WebArchive.class)
                .addClasses(
                    ApplicationInit.class,
                    CustomConverter.class,
                    Servlet.class,
                    TestPojo.class
                ).addAsResource(
                    "META-INF/services/org.eclipse.microprofile.config.spi.Converter"
                ).addAsResource(
                    "META-INF/microprofile-config.properties"
                )
                ;
        
        System.out.println("************************************************************");
        System.out.println(archive.toString(true));
        System.out.println("************************************************************");
        
        return archive;
    }
    
    @Test
    @RunAsClient
    public void testConverter() throws IOException {

        String response = 
                newClient()
                     .target(
                         URI.create(new URL(base, "servlet").toExternalForm()))
                     .request(TEXT_PLAIN)
                     .get(String.class);
        
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Response: " + response);
        System.out.println("-------------------------------------------------------------------------");

        assertTrue(
            response.contains("pojo.value : TestPojo{value=Value for Test Pojo}")
        );
    }
    
}
