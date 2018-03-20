/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 *    Copyright (c) [2018] Payara Foundation and/or its affiliates. All rights reserved.
 * 
 *     The contents of this file are subject to the terms of either the GNU
 *     General Public License Version 2 only ("GPL") or the Common Development
 *     and Distribution License("CDDL") (collectively, the "License").  You
 *     may not use this file except in compliance with the License.  You can
 *     obtain a copy of the License at
 *     https://github.com/payara/Payara/blob/master/LICENSE.txt
 *     See the License for the specific
 *     language governing permissions and limitations under the License.
 * 
 *     When distributing the software, include this License Header Notice in each
 *     file and include the License file at glassfish/legal/LICENSE.txt.
 * 
 *     GPL Classpath Exception:
 *     The Payara Foundation designates this particular file as subject to the "Classpath"
 *     exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 *     file that accompanied this code.
 * 
 *     Modifications:
 *     If applicable, add the following below the License Header, with the fields
 *     enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyright [year] [name of copyright owner]"
 * 
 *     Contributor(s):
 *     If you wish your version of this file to be governed by only the CDDL or
 *     only the GPL Version 2, indicate your decision by adding "[Contributor]
 *     elects to include this software in this distribution under the [CDDL or GPL
 *     Version 2] license."  If you don't indicate a single choice of license, a
 *     recipient has the option to distribute your version of this file under
 *     either the CDDL, the GPL Version 2 or to extend the choice of license to
 *     its licensees as provided above.  However, if you add GPL Version 2 code
 *     and therefore, elected the GPL Version 2 license, then the option applies
 *     only if the new code is made subject to such option by the copyright
 *     holder.
 */
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
import junit.framework.Assert;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.samples12.circuitbreaker.MethodLevelCircuitBreakerBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
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
