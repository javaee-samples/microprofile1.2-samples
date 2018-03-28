package org.eclipse.microprofile.samples12.timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * A bean demonstrating how the Timeout and Asynchronous annotations can be used together.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
@Timeout
public class AsynchronousTimeoutBean {
    
    public static final long AWAIT = 3000;

    /**
     * Example method that simulates a long running query, running in a different thread.
     * @param shouldTimeout
     * @return 
     */
    @Asynchronous
    public Future<Boolean> timeout(boolean shouldTimeout) {
        Boolean timedOut = false;
        
        if (shouldTimeout) {
            try {
                // Simulate a long running query
                Thread.sleep(AWAIT);
            } catch (InterruptedException ex) {
                timedOut = true;
            }
        }
        
        return CompletableFuture.completedFuture(timedOut);
    }
}
