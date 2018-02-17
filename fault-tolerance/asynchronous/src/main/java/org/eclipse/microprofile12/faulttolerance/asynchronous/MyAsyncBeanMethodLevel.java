package org.eclipse.microprofile12.faulttolerance.asynchronous;

import static java.lang.Thread.sleep;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.logging.Level.SEVERE;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Asynchronous;

/**
 * @author Arun Gupta
 * @author Arjan Tijms
 */
@ApplicationScoped
public class MyAsyncBeanMethodLevel {

    public static final long AWAIT = 3000;

    @Asynchronous
    public Future<Integer> addNumbers(int n1, int n2) {
        try {
            // Simulating a long running query
            sleep(AWAIT);
        } catch (InterruptedException ex) {
            Logger.getLogger(MyAsyncBeanMethodLevel.class.getName()).log(SEVERE, null, ex);
        }
        
        return completedFuture(n1 + n2);
    }

}