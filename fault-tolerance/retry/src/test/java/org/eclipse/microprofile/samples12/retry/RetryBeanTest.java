package org.eclipse.microprofile.samples12.retry;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the RetryBean class.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class RetryBeanTest {

    @Inject
    private RetryBean retryBean;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(RetryBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Test that the method retries the expected number of times.
     */
    @Test
    public void retryTest() {
        int numberOfAttempts = 0;
        numberOfAttempts = retryBean.demonstrateRetry();
        
        Assert.assertTrue("Didn't get the expected number of attempts: " + numberOfAttempts, 
                numberOfAttempts == RetryBean.expectedAttempts);
    }

    /**
     * Test that the method aborts on specific errors.
     */
    @Test
    public void abortOnTest() {
        // Prove that the method doesn't just exit anyway by making it retry on an allowed exception.
        int numberOfAttempts = 0;
        try {
            numberOfAttempts = retryBean.demonstrateAbort(false);
        } catch (InterruptedException ie) {
            Assert.fail("Got an unexpected InterruptedException");
        }
        
        Assert.assertTrue("Didn't get the expected number of attempts: " + numberOfAttempts, 
                numberOfAttempts == RetryBean.expectedAttempts);
        
        // Reset the counters
        retryBean.resetCounters();
        
        // Test that it exists on the specified exception
        try {
            retryBean.demonstrateAbort(true);
        } catch (InterruptedException ex) {
            return;
        }
        
        Assert.fail("Didn't get an InterruptedException when expected");
    }
}
