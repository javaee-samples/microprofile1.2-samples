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
import junit.framework.Assert;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.samples12.bulkhead.MethodLevelBulkheadBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
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
