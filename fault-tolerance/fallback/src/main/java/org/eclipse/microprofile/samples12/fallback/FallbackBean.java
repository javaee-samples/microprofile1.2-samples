/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 *    Copyright (c) [2018] Payara Foundation and/or its affiliates. All rights reserved.
 * 
 *     The contents of this file are subject to the terms of either the GNU
 *     General Public License Version 2 only ("GPL") or the Common Development
 *     and Distribution License("CDDL") (collectively, the "License").  You
 *     may not use this file except in compliance with the License.  You can
 *     obtain a copy of the License at
 *     https://github.com/payara/Payara/blob/master/LICENSE.txt
 *     See the License for the specific
 *     language governing permissions and limitations under the License.
 * 
 *     When distributing the software, include this License Header Notice in each
 *     file and include the License file at glassfish/legal/LICENSE.txt.
 * 
 *     GPL Classpath Exception:
 *     The Payara Foundation designates this particular file as subject to the "Classpath"
 *     exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 *     file that accompanied this code.
 * 
 *     Modifications:
 *     If applicable, add the following below the License Header, with the fields
 *     enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyright [year] [name of copyright owner]"
 * 
 *     Contributor(s):
 *     If you wish your version of this file to be governed by only the CDDL or
 *     only the GPL Version 2, indicate your decision by adding "[Contributor]
 *     elects to include this software in this distribution under the [CDDL or GPL
 *     Version 2] license."  If you don't indicate a single choice of license, a
 *     recipient has the option to distribute your version of this file under
 *     either the CDDL, the GPL Version 2 or to extend the choice of license to
 *     its licensees as provided above.  However, if you add GPL Version 2 code
 *     and therefore, elected the GPL Version 2 license, then the option applies
 *     only if the new code is made subject to such option by the copyright
 *     holder.
 */
package org.eclipse.microprofile.samples12.fallback;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 *
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@ApplicationScoped
public class FallbackBean {
    
    public static final String defaultResponse = "I didn't fail!";
    public static final String expectedResponse = "I fell back! Thank you MicroProfile!";
    
    @Retry(maxRetries = 1)
    @Fallback(StringFallbackHandler.class)
    public String demonstrateFallbackHandler(boolean fallback) {
        if (fallback) {
            throw new RuntimeException("I failed somehow! Save me MicroProfile!");
        } else {
            return defaultResponse;
        }
    }
    
    @Retry(maxRetries = 1)
    @Fallback(fallbackMethod = "fallbackMethodExample")
    public String demonstrateFallbackMethod(boolean fallback) {
        if (fallback) {
            throw new RuntimeException("I failed somehow! Save me MicroProfile!");
        } else {
            return defaultResponse;
        }
    }
    
    public String fallbackMethodExample(boolean fallback) {
        return expectedResponse;
    }
    
}
