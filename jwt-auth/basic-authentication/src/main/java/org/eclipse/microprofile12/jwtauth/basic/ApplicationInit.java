package org.eclipse.microprofile12.jwtauth.basic;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.auth.LoginConfig;

@LoginConfig(
    authMethod = "MP-JWT",
    // Even though specified being only for HTTP Basic auth, JBoss/WildFly/Swarm mandates this
    // to refer to its proprietary "security domain" concept.
    realmName = "MP-JWT"
)
@ApplicationScoped
public class ApplicationInit {
    
   
}
