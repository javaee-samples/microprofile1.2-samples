package org.eclipse.microprofile.samples12.bulkhead;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

/**
 * A Bean with a Bulkhead applied at the method level, demonstrating how you can specify different bulkhead values for
 * each method.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
public class MethodLevelBulkheadBean {

    public static final long AWAIT = 3000;
    
    /**
     * Example method that simulates a long running query
     */
    @Bulkhead(5)
    public void method1() {
        try {
            // Simulate a long running query
            Thread.sleep(AWAIT);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClassLevelBulkheadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Example method that simulates a long running query with its own bulkhead.
     */
    @Bulkhead(3)
    public void method2() {
        try {
            // Simulate a long running query
            Thread.sleep(AWAIT);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClassLevelBulkheadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}