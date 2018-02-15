/** Copyright Payara Services Limited **/

package org.eclipse.microprofile12.metrics.application;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.WebTarget;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Gaurav Gupta
 */
@RunWith(Arquillian.class)
public class ApplicationMetricsTest {

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive
                = create(WebArchive.class)
                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                        .addClass(ApplicationInit.class)
                        .addClass(UserResource.class);

        System.out.println("************************************************************");
        System.out.println(archive.toString(true));
        System.out.println("************************************************************");

        return archive;
    }
    
    @Before
    public void init() throws IOException{
        invokeUserResource();
    }

    @Test
    @InSequence(1)
    @RunAsClient
    public void testBasePrometheusMetrics() throws IOException {
        Response response
                = getApplicationMetricTarget()
                        .request(TEXT_PLAIN)
                        .get();
        String responseText = response.readEntity(String.class);
        System.out.println("testBasePrometheusMetrics " + responseText);
        assertTrue(responseText.contains("# TYPE application:org_eclipse_microprofile12_metrics_application_user_resource_get_users counter"));
        assertTrue(responseText.contains("# TYPE application:org_eclipse_microprofile12_metrics_application_user_resource_get_user_count gauge"));
    }

    @Test
    @InSequence(2)
    @RunAsClient
    public void testBaseJsonMetrics() throws IOException {
        Response response
                = getApplicationMetricTarget()
                        .request(APPLICATION_JSON)
                        .get();
        JsonObject jsonObject = Json
                .createReader(new StringReader(response.readEntity(String.class)))
                .readObject();
        
        assertEquals(jsonObject.getInt("org.eclipse.microprofile12.metrics.application.UserResource.getUsers"), 2);
        assertEquals(jsonObject.getInt("org.eclipse.microprofile12.metrics.application.UserResource.getUserCount"), 5);
    }

    private WebTarget getApplicationMetricTarget() throws IOException {
        return newClient().target(URI.create(new URL(base, "/metrics/application").toExternalForm()));
    }
    
    private void invokeUserResource() throws IOException{
        //invoke UserResource.getUsers
        newClient()
                .target(URI.create(new URL(base, "user/all").toExternalForm()))
                .request(APPLICATION_JSON)
                .get();
        
        //invoke UserResource.getUserCount
        newClient()
                .target(URI.create(new URL(base, "user/count").toExternalForm()))
                .request(APPLICATION_JSON)
                .get();
    }

}
