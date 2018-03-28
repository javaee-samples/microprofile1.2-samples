package org.eclipse.microprofile.samples12.fallback;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

/**
 * An example fallback handler for a method with a return type of String.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
public class StringFallbackHandler implements FallbackHandler<String> {

    @Override
    public String handle(ExecutionContext ec) {
        return FallbackBean.expectedResponse;
    }
    
}
