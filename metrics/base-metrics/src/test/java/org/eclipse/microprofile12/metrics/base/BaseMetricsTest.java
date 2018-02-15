/** Copyright Payara Services Limited **/

package org.eclipse.microprofile12.metrics.base;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.WebTarget;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Gaurav Gupta
 */
@RunWith(Arquillian.class)
public class BaseMetricsTest {

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive
                = create(WebArchive.class)
                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        System.out.println("************************************************************");
        System.out.println(archive.toString(true));
        System.out.println("************************************************************");

        return archive;
    }

    @Test
    @RunAsClient
    public void testBasePrometheusMetrics() throws IOException {
        Response response
                = getBaseMetricTarget()
                .request(TEXT_PLAIN)
                .get();
        String responseText = response.readEntity(String.class);
        assertTrue(responseText.contains("# TYPE base:thread_max_count"));
        assertTrue(responseText.contains("# TYPE base:thread_count"));
        assertTrue(responseText.contains("# TYPE base:cpu_system_load_average"));
        assertTrue(responseText.contains("# TYPE base:jvm_uptime"));
    }

    @Test
    @RunAsClient
    public void testBaseJsonMetrics() throws IOException {
        Response response 
                = getBaseMetricTarget()
                .request(APPLICATION_JSON)
                .get();
        JsonObject jsonObject = Json
                .createReader(new StringReader(response.readEntity(String.class)))
                .readObject();
        assertTrue(jsonObject.containsKey("thread.max.count"));
        assertTrue(jsonObject.containsKey("thread.count"));
        assertTrue(jsonObject.containsKey("cpu.systemLoadAverage"));
        assertTrue(jsonObject.containsKey("jvm.uptime"));
    }

    private WebTarget getBaseMetricTarget() throws MalformedURLException {
        return newClient().target(URI.create(new URL(base, "/metrics/base").toExternalForm()));
    }

}
