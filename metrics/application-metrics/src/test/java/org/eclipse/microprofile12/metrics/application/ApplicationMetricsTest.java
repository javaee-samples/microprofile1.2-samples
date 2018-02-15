/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) [2018] Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */


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
