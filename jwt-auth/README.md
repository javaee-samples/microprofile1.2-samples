# Eclipse MicroProfile 1.2 Samples - JWT Auth 1.0


## Samples ##

 - **basic-authentication** The test sends a very basic signed JWT token to a protected servlet. The MP-JWT Auth implementation
   checks if the token is valid and sets the authenticated identity from the `upn` field and `groups` field
   


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
     
