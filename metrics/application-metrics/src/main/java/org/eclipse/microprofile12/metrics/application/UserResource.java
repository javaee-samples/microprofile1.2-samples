/** Copyright Payara Services Limited **/

package org.eclipse.microprofile12.metrics.application;

import static java.util.Arrays.asList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;

/**
 * @author Gaurav Gupta
 */
@ApplicationScoped
@Path("/user")
public class UserResource {
    
    private final List<String> users = asList("Arjan", "Gaurav", "Mert", "Mike", "Ondrej");
        
    @GET
    @Path("/all")
    @Counted(monotonic = true)
    public Response getUsers() {
        return Response
                .ok(users)
                .build();
    }
    
    @GET
    @Path("/count")
    @Gauge(unit = MetricUnits.NONE)
    public int getUserCount() {
        return users.size();
    }

}
