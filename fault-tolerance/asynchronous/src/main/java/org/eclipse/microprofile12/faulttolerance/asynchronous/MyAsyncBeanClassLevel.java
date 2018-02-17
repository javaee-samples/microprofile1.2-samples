package org.eclipse.microprofile12.faulttolerance.asynchronous;

import static java.lang.Thread.sleep;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Asynchronous;

/**
 * @author Arun Gupta
 * @author Arjan Tijms
 */
@Asynchronous
@ApplicationScoped
public class MyAsyncBeanClassLevel {

    public static final long AWAIT = 3000;

    public Future<Integer> addNumbers(int n1, int n2) {
        try {
            // simulating a long running query
            sleep(AWAIT);
        } catch (InterruptedException ex) {
            Logger.getLogger(MyAsyncBeanClassLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return completedFuture(n1 + n2);
    }

}
