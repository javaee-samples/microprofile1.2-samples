package org.eclipse.microprofile.samples12.retry;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * A bean demonstrating the usage of the Retry annotation.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
@Retry(maxRetries = 8, delay = 500, jitter = 250)
public class RetryBean {
    
    public static final int expectedAttempts = 5;
    
    // Counters for each method, used to count the number of attempts - this is most definitely not thread safe!
    private int method1Counter = 0;
    private int method2Counter = 0;
    
    /**
     * Example method that demonstrates the retry annotation by only succeeded once the counter has reached a 
     * certain point.
     * @return 
     */
    public int demonstrateRetry() {
        if (method1Counter < expectedAttempts) {
            method1Counter++;
            throw new RuntimeException("Oh noes! Some exception! Method Counter is currently: " + method1Counter);
        }
        
        return method1Counter;
    }
    
    /**
     * An example method demonstrating how you can configure the retry annotation to abort on certain exceptions.
     * @param abort
     * @return
     * @throws InterruptedException Not actually interrupted, just used as an example
     */
    @Retry(maxRetries = 5, abortOn = InterruptedException.class)
    public int demonstrateAbort(boolean abort) throws InterruptedException {
        if (method2Counter < expectedAttempts) {
            method2Counter++;
            
            if (abort) {
                throw new InterruptedException("Oh noes! A more serious exception!");
            } else {
                throw new RuntimeException("Oh noes! Some exception! Method Counter is currently: " + method2Counter);
            }
        }
        
        return method2Counter;
    }
    
    /**
     * Helper method used to reset the counters.
     */
    public void resetCounters() {
        method1Counter = 0;
        method2Counter = 0;
    }
}
