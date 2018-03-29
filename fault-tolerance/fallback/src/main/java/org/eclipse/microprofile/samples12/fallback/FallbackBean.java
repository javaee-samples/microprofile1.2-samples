package org.eclipse.microprofile.samples12.fallback;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * A bean demonstrating a simple use case of utilising a fallback handler or a fallback method with the Retry annotation.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
public class FallbackBean {
    
    public static final String defaultResponse = "I didn't fail!";
    public static final String expectedResponse = "I fell back! Thank you MicroProfile!";
    
    /**
     * Example method that can be made to throw an exception, kicking off the fallbackHandler after it has retried once.
     * @param fallback
     * @return 
     */
    @Retry(maxRetries = 1)
    @Fallback(StringFallbackHandler.class)
    public String demonstrateFallbackHandler(boolean fallback) {
        if (fallback) {
            throw new RuntimeException("I failed somehow! Save me MicroProfile!");
        } else {
            return defaultResponse;
        }
    }
    
    /**
     * Example method that can be made to throw an exception, kicking off the fallback method after it has retried once.
     * @param fallback
     * @return 
     */
    @Retry(maxRetries = 1)
    @Fallback(fallbackMethod = "fallbackMethodExample")
    public String demonstrateFallbackMethod(boolean fallback) {
        if (fallback) {
            throw new RuntimeException("I failed somehow! Save me MicroProfile!");
        } else {
            return defaultResponse;
        }
    }
    
    /**
     * Very basic example of a fallback method.
     * @param fallback Unused in this example method, but necessary because a fallback method must share the same return
     * type and parameters as the method it is the fallback for
     * @return 
     */
    public String fallbackMethodExample(boolean fallback) {
        return expectedResponse;
    }
    
}
