package org.eclipse.microprofile.samples12.timeout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for the AsynchronousTimeoutBean.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class AsynchronousTimeoutBeanTest {
    
    @Inject
    private AsynchronousTimeoutBean asynchronousTimeoutBean;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClasses(AsynchronousTimeoutBean.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource("project-defaults.yml");
    }
    
    /**
     * Tests that the timeout annotation interrupts a long running task and throws a TimeoutException upon calling
     * future.get().
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    @Test
    public void timeoutTest() throws InterruptedException, ExecutionException {
        Future<Boolean> future = asynchronousTimeoutBean.timeout(true);
        try {
            future.get();
        } catch (TimeoutException toe) {
            return;
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof TimeoutException) {
                return;
            }
        }
        
        Assert.fail();
    }
}
