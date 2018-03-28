package org.eclipse.microprofile.samples12.bulkhead;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

/**
 * A Bean with both a Bulkhead and Asynchronous annotation applied. This combination means that the Bulkhead will allow
 * a specified number of tasks to queue for execution.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@Asynchronous
@Bulkhead(value = 3, waitingTaskQueue = 5)
@ApplicationScoped
public class AsynchronousBulkheadBean {
    
    public static final long AWAIT = 3000;

    /**
     * Example method that simulates a long running task.
     * @return 
     */
    public Future<String> method1() {
        try {
            // Simulate a long running query
            Thread.sleep(AWAIT);
        } catch (InterruptedException ex) {
            Logger.getLogger(AsynchronousBulkheadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return CompletableFuture.completedFuture("Wibbles");
    }
}
