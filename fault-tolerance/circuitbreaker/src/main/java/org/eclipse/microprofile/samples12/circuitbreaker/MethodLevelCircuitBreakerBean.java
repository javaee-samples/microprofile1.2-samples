package org.eclipse.microprofile.samples12.circuitbreaker;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

/**
 * A bean with a Circuitbreaker applied at the method level.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
public class MethodLevelCircuitBreakerBean {
    public static final String EXPECTED_ERROR_MESSAGE = "Method failed to execute";
    
    /**
     * Example method that can be made to throw an exception.
     * @param shouldThrowException 
     */
    @CircuitBreaker(requestVolumeThreshold = 6)
    public void throwException(boolean shouldThrowException) {
        if (shouldThrowException) {
            throw new RuntimeException(EXPECTED_ERROR_MESSAGE);
        }
    }
}
