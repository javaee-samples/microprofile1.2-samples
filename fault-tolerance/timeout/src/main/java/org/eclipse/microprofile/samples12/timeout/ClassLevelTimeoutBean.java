package org.eclipse.microprofile.samples12.timeout;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * A bean demonstrating how to use the Timeout annotation.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
@Timeout(1500)
public class ClassLevelTimeoutBean {

    public static final long AWAIT = 3000;

    /**
     * Example method that simulates a long running query.
     * @param shouldTimeout 
     */
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
