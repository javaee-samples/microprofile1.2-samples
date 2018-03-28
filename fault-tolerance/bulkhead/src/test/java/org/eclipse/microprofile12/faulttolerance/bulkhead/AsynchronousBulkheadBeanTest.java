package org.eclipse.microprofile12.faulttolerance.bulkhead;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.samples12.bulkhead.AsynchronousBulkheadBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for the AsynchronousBulkheadBean class.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class AsynchronousBulkheadBeanTest {
    
    @Inject
    AsynchronousBulkheadBean asynchronousBulkheadBean;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClasses(AsynchronousBulkheadBean.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    /**
     * Test that the bulkhead begins queueing task if more than the maximum number of threads are executing concurrently.
     * @throws NoSuchMethodException 
     */
    @Test
    public void bulkheadQueueTest() throws NoSuchMethodException {
        int numberOfExpectedFailures = 0;
        
        // Kick off more tasks than the bulkhead value, but less than the combined total, causing the bulkhead to block 
        // and start queuing
        int numberOfTasks = AsynchronousBulkheadBean.class.getAnnotation(Bulkhead.class).value() 
                + AsynchronousBulkheadBean.class.getAnnotation(Bulkhead.class).waitingTaskQueue()
                - 1;
        
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfTasks; i++) {
            futures.add(asynchronousBulkheadBean.method1());
        }
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(AsynchronousBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                Logger.getLogger(AsynchronousBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                Assert.fail("Got an unexpected ExecutionException");
            } catch (BulkheadException be) {
                failures ++;
            }
        }
        
        // numberOfExpectedFailures tasks should fail
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
    }
    
    /**
     * Test that when the bulkhead queue is full, errors start to get thrown.
     * @throws NoSuchMethodException 
     */
    @Test
    public void bulkheadLimitTest() throws NoSuchMethodException {
        int numberOfExpectedFailures = 2;
        
        // Kick off more tasks than the bulkhead value and queue combined, causing the bulkhead to block 
        // and start queuing, before throwing an exception
        int numberOfTasks = AsynchronousBulkheadBean.class.getAnnotation(Bulkhead.class).value() 
                + AsynchronousBulkheadBean.class.getAnnotation(Bulkhead.class).waitingTaskQueue() 
                + numberOfExpectedFailures;
        
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfTasks; i++) {
            futures.add(asynchronousBulkheadBean.method1());
        }
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(AsynchronousBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                Logger.getLogger(AsynchronousBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                Assert.fail("Got an unexpected ExecutionException");
            } catch (BulkheadException be) {
                failures ++;
            }
        }
        
        // numberOfExpectedFailures tasks should fail
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
    }
    
}
