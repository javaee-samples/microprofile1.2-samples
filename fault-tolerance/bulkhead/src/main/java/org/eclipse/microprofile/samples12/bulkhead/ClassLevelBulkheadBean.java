package org.eclipse.microprofile.samples12.bulkhead;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

/**
 * A Bean with a Bulkhead applied at the class level with default values. The Bulkhead will be applied to
 * every method in this class, only allowing 10 threads to access them (individually - they do not share 
 * bulkhead tokens) concurrently, with no queueing allowed.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@Bulkhead
@ApplicationScoped
public class ClassLevelBulkheadBean {

    public static final long AWAIT = 3000;

    /**
     * Example method that simulates a long running query
     */
    public void method1() {
        try {
            // Simulate a long running query
            Thread.sleep(AWAIT);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClassLevelBulkheadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Example method that simulates a long running query
     */
    public void method2() {
        try {
            // Simulate a long running query
            Thread.sleep(AWAIT);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClassLevelBulkheadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
