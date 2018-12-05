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
import org.eclipse.microprofile.samples12.circuitbreaker.ClassLevelCircuitBreakerBean;
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
 * Test class for ClassLevelCircuitBreakerBean.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class ClassLevelCircuitBreakerBeanTest {
    
    @Inject
    private ClassLevelCircuitBreakerBean classLevelCircuitBreakerBean;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClasses(ClassLevelCircuitBreakerBean.class)
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
                ClassLevelCircuitBreakerBean.class.getAnnotation(CircuitBreaker.class).delay(), 
                ClassLevelCircuitBreakerBean.class.getAnnotation(CircuitBreaker.class).delayUnit()).toMillis() 
                + addedWaitTimeMillis;
        Thread.sleep(timeToWaitMillis);
        
        // Fill it with goodness
        int numberOfTasks = ((Double) (
                ClassLevelCircuitBreakerBean.class.getAnnotation(CircuitBreaker.class).requestVolumeThreshold() 
                * ClassLevelCircuitBreakerBean.class.getAnnotation(CircuitBreaker.class).failureRatio())).intValue()
                + ClassLevelCircuitBreakerBean.class.getAnnotation(CircuitBreaker.class).successThreshold();
        executeThrowExceptionMethodAsynchronously(numberOfTasks, false);
                
    }
    
    /**
     * Test that the Circuitbreaker opens once enough failures have been thrown.
     */
    @Test
    public void circuitBreakerOpensTest() {
        int numberOfExpectedFailures = 2;
        
        int numberOfTasks = ((Double) (
                ClassLevelCircuitBreakerBean.class.getAnnotation(CircuitBreaker.class).requestVolumeThreshold() 
                * ClassLevelCircuitBreakerBean.class.getAnnotation(CircuitBreaker.class).failureRatio()))
                .intValue() + numberOfExpectedFailures 
                + 1; //We need one more after circuit opens to start getting expected number of failures
        
        // Throw errors to to open the circuit
        List<Future<String>> futures = executeThrowExceptionMethodAsynchronously(numberOfTasks, true);
        
        // Await and collect results to make sure everything has completed or thrown an exception
        int failures = 0;
        for (Future<String> future : futures) {            
            try {
                future.get();
            } catch (InterruptedException ie) {
                Logger.getLogger(ClassLevelCircuitBreakerBeanTest.class.getName()).log(Level.SEVERE, null, ie);
                Assert.fail("Got an unexpected InterruptedException");
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof CircuitBreakerOpenException) {
                    failures ++;
                } else if (ex.getCause() instanceof RuntimeException 
                        && ex.getCause().getMessage().equals(ClassLevelCircuitBreakerBean.EXPECTED_ERROR_MESSAGE)) {
                    // Om nom nom
                } else {
                    Logger.getLogger(ClassLevelCircuitBreakerBeanTest.class.getName()).log(Level.SEVERE, null, ex);
                    Assert.fail("Got an unexpected ExecutionException");
                }
            }
        }
        
        // numberOfExpectedFailures tasks should fail
        Assert.assertTrue("Did not get " + numberOfExpectedFailures + ": " + failures, 
                failures == numberOfExpectedFailures);
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
        
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        
        Callable<String> task = () -> { 
            classLevelCircuitBreakerBean.throwException(shouldThrowException);
            return null;
        };
        
        for (int i = 0; i < iterations; i++) {
            futures.add(executorService.submit(task));
        }
        
        return futures;
    }
}
