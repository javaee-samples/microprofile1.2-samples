package org.eclipse.microprofile.samples12.timeout;

import javax.inject.Inject;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for ClassLevelTimeoutBean.
 * @author Andrew Pielage <andrew.pielage@payara.fish>
 */
@RunWith(Arquillian.class)
public class ClassLevelTimeoutBeanTest {
    
    @Inject
    private ClassLevelTimeoutBean classLevelTimeoutBean;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClasses(ClassLevelTimeoutBean.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    /**
     * Tests that the timeout annotation interrupts a long running task and throws a TimeoutException.
     */
    @Test
    public void timeoutTest() {
        try {
            classLevelTimeoutBean.timeout(true);
        } catch (TimeoutException toe) {
            return;
        }
        
        Assert.fail();
    }
}
