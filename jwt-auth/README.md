# Eclipse MicroProfile 1.2 Samples - JWT Auth 1.0

 - [Wiki project page](https://wiki.eclipse.org/MicroProfile/JWT_Auth)
 - [Spec, API, TCK GitHub repo](https://github.com/eclipse/microprofile-jwt-auth)

**Note** for practical purposes these samples have been superseded by the ones at https://github.com/javaee-samples/microprofile1.4-samples/tree/master/jwt-auth

## Samples ##

 - **basic-authentication** The test sends a very basic signed JWT token to a protected servlet. The MP-JWT Auth implementation
   checks if the token is valid and sets the authenticated identity from the `upn` field and `groups` field
   **jaxrs ** Just like basic-authentication, but uses a JAX-RS endpoint. Specifically demonstrates the support of @RolesAllowed to secure and endpoint.
   


## Implementation config ##

There's at least 2 items that need to be configured in an implementation specific way: the *issuer*, which represents the party that vended the token, and the *public key* which is used to verify the signature of a JWT token being sent to the server.

 - **Payara (tech preview)** 
     - *issuer* - defined in "payara-mp-jwt.properties" which is placed in the classpath root of the application archive 
     - *public key* - the file "publicKey.pem" which is also placed in the classpath root of the application archive
 - **Liberty**
     - *issuer* - defined in the "mpJwt" tag in "server.xml" which in the placed inside the installed Liberty: `[install dir]/wlp/usr/servers/defaultServer/server.xml`
     - *public key* - the file "key.jks" which in the placed inside the installed Liberty: `[install dir]/wlp/usr/servers/defaultServer/resources/security/key.jks`
- **WildFly (Swarm)**
     - *issuer* - defined in "project-default.yml" which is placed in the classpath root of the application archive. This also configures the security system (security domain in JBoss terms) such that all artifacts to support MP-Auth JWT are installed
     - *public key* - the file "MP-JWT-SIGNER" which is placed in the classpath's META-INF of the application archive.
     
     
## TCK ##

The public/private keys are taken from the MP-Auth TCK. The Liberty key is in a special format and has been taken from the Liberty TCK version. Config files for the various servers are inspired by those in the TCK and TCK extensions.

See the following URLs:

 - [MP-Auth TCK](https://github.com/eclipse/microprofile-jwt-auth/tree/master/tck)
 - [Payara TCK Ext](https://github.com/payara/Payara/tree/Payara-5/appserver/payara-appserver-modules/microprofile/jwt-auth-tck)
 - [Liberty TCK](https://github.com/OpenLiberty/open-liberty/tree/master/dev/com.ibm.ws.security.mp.jwt_fat_tck)
 - [WildFly TCK Ext](https://github.com/MicroProfileJWT/wfswarm-jwt-auth-tck)

