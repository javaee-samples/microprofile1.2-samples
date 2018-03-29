package org.eclipse.microprofile.samples12.fallback;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the FallbackBean class.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class FallbackBeanTest {

    @Inject
    private FallbackBean fallbackBean;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(FallbackBean.class, StringFallbackHandler.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Tests the operation of the fallback handler.
     */
    @Test
    public void fallbackHandlerTest() {
        String result = fallbackBean.demonstrateFallbackHandler(true);
        Assert.assertTrue("Did not get the expected result", result.equals(FallbackBean.expectedResponse));
    }

    /**
     * Tests the operation of the fallback method.
     */
    @Test
    public void fallbackMethodTest() {
        String result = fallbackBean.demonstrateFallbackMethod(true);
        Assert.assertTrue("Did not get the expected result", result.equals(FallbackBean.expectedResponse));
    }
    
}
