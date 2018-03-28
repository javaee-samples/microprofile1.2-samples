package org.eclipse.microprofile.samples12.timeout;

import java.time.temporal.ChronoUnit;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * A bean demonstrating how you can use the Timeout annotation on a specific method.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
public class MethodLevelTimeoutBean {
    
    public static final long AWAIT = 5000;

    /**
     * Example method that simulates a long running query.
     * @param shouldTimeout 
     */
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    public void timeout(boolean shouldTimeout) {
        if (shouldTimeout) {
            try {
                // Simulate a long running query
                Thread.sleep(AWAIT);
            } catch (InterruptedException ex) {
                // Om nom nom
            }
        }
    }
}
