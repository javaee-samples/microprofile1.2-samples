package org.eclipse.microprofile12.faulttolerance.bulkhead;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.samples12.bulkhead.MethodLevelBulkheadBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for the MethodLevelBulkheadBean class.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class MethodLevelBulkheadBeanTest {
    
    @Inject
    MethodLevelBulkheadBean methodLevelBulkheadBean;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClasses(MethodLevelBulkheadBean.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    /**
     * Test that the bulkhead for method1 prevents more than the configured number of tasks to execute concurrently.
     * @throws NoSuchMethodException 
     */
    @Test
    public void method1BulkheadLimitTest() throws NoSuchMethodException {
        int numberOfExpectedFailures = 2;
        
        // Kick off more tasks than the bulkhead value, causing the bulkhead to block
        int numberOfTasks = MethodLevelBulkheadBean.class.getMethod("method1").getAnnotation(Bulkhead.class).value() 
                + numberOfExpectedFailures;
        List<Future<String>> method1Futures = executeMethod1Asynchronously(numberOfTasks);
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : method1Futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(MethodLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof BulkheadException) {
                    failures ++;
                } else {
                    Logger.getLogger(MethodLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                    Assert.fail("Got an unexpected ExecutionException");
                }
            }
        }
        
        // numberOfExpectedFailures tasks should fail
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
    }
    
    /**
     * Test that the bulkhead for method2 prevents more than the configured number of tasks to execute concurrently.
     * @throws NoSuchMethodException 
     */
    @Test
    public void method2BulkheadLimitTest() throws NoSuchMethodException {
        int numberOfExpectedFailures = 2;
        
        // Kick off more tasks than the bulkhead value, causing the bulkhead to block 
        int numberOfTasks = MethodLevelBulkheadBean.class.getMethod("method2").getAnnotation(Bulkhead.class).value() 
                + numberOfExpectedFailures;
        List<Future<String>> method2Futures = executeMethod2Asynchronously(numberOfTasks);
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : method2Futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(MethodLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof BulkheadException) {
                    failures ++;
                } else {
                    Logger.getLogger(MethodLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                    Assert.fail("Got an unexpected ExecutionException");
                }
            }
        }
        
        // numberOfExpectedFailures tasks should fail
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
    }
    
    /**
     * Helper method that kicks off a number of threads to concurrently executes method1.
     * @param iterations
     * @return A list of Futures.
     */
    private List<Future<String>> executeMethod1Asynchronously(int iterations) {
        List<Future<String>> futures = new ArrayList<>();
        
        ExecutorService executorService = Executors.newFixedThreadPool(iterations);
        
        Callable<String> task = () -> { 
            methodLevelBulkheadBean.method1();
            return "Wibbles";
        };
        
        for (int i = 0; i < iterations; i++) {
            futures.add(executorService.submit(task));
        }
        
        return futures;
    }
    
    /**
     * Helper method that kicks off a number of threads to concurrently executes method2.
     * @param iterations
     * @return A list of Futures.
     */
    private List<Future<String>> executeMethod2Asynchronously(int iterations) {
        List<Future<String>> futures = new ArrayList<>();
        
        ExecutorService executorService = Executors.newFixedThreadPool(iterations);
        
        Callable<String> task = () -> { 
            methodLevelBulkheadBean.method2();
            return "Wobbles";
        };
        
        for (int i = 0; i < iterations; i++) {
            futures.add(executorService.submit(task));
        }
        
        return futures;
    }
}
