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
import org.eclipse.microprofile.samples12.bulkhead.ClassLevelBulkheadBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for the ClassLevelBulkheadBean class.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class ClassLevelBulkheadBeanTest {
    
    @Inject
    private ClassLevelBulkheadBean classLevelBulkheadBean;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClasses(ClassLevelBulkheadBean.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    /**
     * Test that the bulkhead prevents more than the configured number of threads to execute on the method concurrently.
     * @throws NoSuchMethodException 
     */
    @Test
    public void bulkheadLimitTest() throws NoSuchMethodException {   
        int numberOfExpectedFailures = 2;
        
        // Kick off more tasks than the bulkhead value, causing the bulkhead to block
        int numberOfTasks = ClassLevelBulkheadBean.class.getAnnotation(Bulkhead.class).value() 
                + numberOfExpectedFailures;
        List<Future<String>> method1Futures = executeMethod1Asynchronously(numberOfTasks);
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : method1Futures) {            
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(ClassLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof BulkheadException) {
                    failures ++;
                } else {
                    Logger.getLogger(ClassLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                    Assert.fail("Got an unexpected ExecutionException");
                }
            }
        }
        
        // numberOfExpectedFailures tasks should fail
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
    }
    
    /**
     * Test that the two methods do not share bulkhead tokens, even though the share the same class level annotation.
     * @throws NoSuchMethodException 
     */
    @Test
    public void methodsDoNotShareClassLevelBulkheadPermitsTest() throws NoSuchMethodException {    
        int numberOfExpectedFailures = 2;
        
        // Kick off more tasks than the bulkhead value for both methods simultaneously, causing the bulkhead to block
        int numberOfTasks = ClassLevelBulkheadBean.class.getAnnotation(Bulkhead.class).value() 
                + numberOfExpectedFailures;
        List<Future<String>> method1Futures = executeMethod1Asynchronously(numberOfTasks);
        List<Future<String>> method2Futures = executeMethod2Asynchronously(numberOfTasks);
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : method1Futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(ClassLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof BulkheadException) {
                    failures ++;
                } else {
                    Logger.getLogger(ClassLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                    Assert.fail("Got an unexpected ExecutionException");
                }
            }
        }
        
        for (Future<String> future : method2Futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(ClassLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof BulkheadException) {
                    failures ++;
                } else {
                    Logger.getLogger(ClassLevelBulkheadBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                    Assert.fail("Got an unexpected ExecutionException");
                }
            }
        }
        
        // Since we're doing this against two methods with separate bulkheads, they should not share bulkhead tokens,
        // meaning we should get numberOfExpectedFailures x 2
        numberOfExpectedFailures = numberOfExpectedFailures * 2;
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
    }
    
    /**
     * Helper method that kicks off a number of threads to concurrently execute the method1 method
     * @param iterations
     * @return A list of Future results
     */
    private List<Future<String>> executeMethod1Asynchronously(int iterations) {
        List<Future<String>> futures = new ArrayList<>();
        
        ExecutorService executorService = Executors.newFixedThreadPool(iterations);
        
        Callable<String> task = () -> { 
            classLevelBulkheadBean.method1();
            return "Wibbles";
        };
        
        for (int i = 0; i < iterations; i++) {
            futures.add(executorService.submit(task));
        }
        
        return futures;
    }
    
    /**
     * Helper method that kicks off a number of threads to concurrently execute the method2 method
     * @param iterations
     * @return A list of Future results
     */
    private List<Future<String>> executeMethod2Asynchronously(int iterations) {
        List<Future<String>> futures = new ArrayList<>();
        
        ExecutorService executorService = Executors.newFixedThreadPool(iterations);
        
        Callable<String> task = () -> { 
            classLevelBulkheadBean.method2();
            return "Wobbles";
        };
        
        for (int i = 0; i < iterations; i++) {
            futures.add(executorService.submit(task));
        }
        
        return futures;
    }
    
}
