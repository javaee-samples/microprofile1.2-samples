package org.eclipse.microprofile.samples12.circuitbreaker;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

/**
 * A bean with a Circuitbreaker applied at the class level.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@CircuitBreaker(requestVolumeThreshold = 4)
@ApplicationScoped
public class ClassLevelCircuitBreakerBean {
    
    public static final String EXPECTED_ERROR_MESSAGE = "Method failed to execute";
    
    /**
     * Example method that can be made to throw an exception.
     * @param shouldThrowException Whether an exception should be thrown or not
     */
    public void throwException(boolean shouldThrowException) {
        if (shouldThrowException) {
            throw new RuntimeException(EXPECTED_ERROR_MESSAGE);
        }
    }
}
