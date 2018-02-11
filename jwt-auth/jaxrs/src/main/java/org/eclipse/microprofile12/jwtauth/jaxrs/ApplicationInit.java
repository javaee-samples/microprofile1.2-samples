package org.eclipse.microprofile12.jwtauth.jaxrs;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;

@LoginConfig(
    authMethod = "MP-JWT",
    // Even though specified being only for HTTP Basic auth, JBoss/WildFly/Swarm mandates this
    // to refer to its proprietary "security domain" concept.
    realmName = "MP-JWT"
)
@DeclareRoles({"architect", "bar", "kaz"})
@ApplicationPath("/")
public class ApplicationInit extends Application {
    
   
}
