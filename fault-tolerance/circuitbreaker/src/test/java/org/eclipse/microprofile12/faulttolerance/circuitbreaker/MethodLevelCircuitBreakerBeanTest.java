package org.eclipse.microprofile12.faulttolerance.circuitbreaker;

import java.time.Duration;
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
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.samples12.circuitbreaker.MethodLevelCircuitBreakerBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for MethodLevelCircuitBreakerBean.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class MethodLevelCircuitBreakerBeanTest {
    
    @Inject
    private MethodLevelCircuitBreakerBean methodLevelCircuitBreakerBean;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClasses(MethodLevelCircuitBreakerBean.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    /**
     * When running against a remote profile, we need to wait for the circuitbreaker to reset between each test.
     * @throws NoSuchMethodException
     * @throws InterruptedException 
     */
    @Before
    public void resetCircuitBreaker() throws NoSuchMethodException, InterruptedException {
        // Wait to half-open the circuit breaker - previous tests may have run and left it in a sorry state
        long addedWaitTimeMillis = 3000;
        long timeToWaitMillis = Duration.of(
                MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).delay(), 
                MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).delayUnit()).toMillis() + addedWaitTimeMillis;
        Thread.sleep(timeToWaitMillis);
        
        // Fill it with goodness
        int numberOfTasks = ((Double) (
                MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).requestVolumeThreshold() 
                * MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).failureRatio())).intValue()
                + MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).successThreshold();
        executeThrowExceptionMethodAsynchronously(numberOfTasks, false);
    }
    
    /**
     * Test that once a circuitbreaker has opened, that it recloses itself and allows executions again.
     * @throws NoSuchMethodException
     * @throws InterruptedException 
     */
    @Test
    public void circuitBreakerReclosesTest() throws NoSuchMethodException, InterruptedException {
        int numberOfExpectedFailures = 2;
        
        int numberOfTasks = ((Double) (
                MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).requestVolumeThreshold() 
                * MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).failureRatio()))
                .intValue() + numberOfExpectedFailures;
        
        // Force the method to throw errors to open the circuit
        List<Future<String>> futures = executeThrowExceptionMethodAsynchronously(numberOfTasks, true);
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : futures) {            
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(MethodLevelCircuitBreakerBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof CircuitBreakerOpenException) {
                    failures ++;
                } else if (ex.getCause() instanceof RuntimeException 
                        && ex.getCause().getMessage().equals(MethodLevelCircuitBreakerBean.EXPECTED_ERROR_MESSAGE)) {
                    // Om nom nom
                } else {
                    Logger.getLogger(MethodLevelCircuitBreakerBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                    Assert.fail("Got an unexpected ExecutionException");
                }
            }
        }
        
        // numberOfExpectedFailures tasks should fail
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
        
        // Now we wait...
        long addedWaitTimeMillis = 3000;
        long timeToWaitMillis = Duration.of(
                MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).delay(), 
                MethodLevelCircuitBreakerBean.class.getMethod("throwException", boolean.class)
                        .getAnnotation(CircuitBreaker.class).delayUnit()).toMillis() + addedWaitTimeMillis;
        Thread.sleep(timeToWaitMillis);
        
        // We should now be able to send a message though as the circuitbreaker should be half-open
        try {
            methodLevelCircuitBreakerBean.throwException(false);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Logger.getLogger(MethodLevelCircuitBreakerBeanTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Got an unexpected Exception");
        }
    }
    
    /**
     * Helper method that spawns a number of threads to execute against the throwException method concurrently.
     * @param iterations
     * @param shouldThrowException
     * @return 
     */
    private List<Future<String>> executeThrowExceptionMethodAsynchronously(int iterations, 
            boolean shouldThrowException) {
        List<Future<String>> futures = new ArrayList<>();
        
        ExecutorService executorService = Executors.newFixedThreadPool(iterations);
        
        Callable<String> task = () -> { 
            methodLevelCircuitBreakerBean.throwException(shouldThrowException);
            return null;
        };
        
        for (int i = 0; i < iterations; i++) {
            futures.add(executorService.submit(task));
        }
        
        return futures;
    }
}
