package org.eclipse.microprofile12.faulttolerance.asynchronous;

import static com.jayway.awaitility.Awaitility.await;
import static java.lang.System.currentTimeMillis;
import static org.eclipse.microprofile12.Libraries.awaitability;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jakub Marchwicki
 * @author Arjan Tijms
 */
@RunWith(Arquillian.class)
public class AsyncMethodBeanTest {

    @Inject
    private MyAsyncBeanMethodLevel bean;

    @Deployment
    public static WebArchive createDeployment() {
        return create(WebArchive.class)
                    .addAsLibraries(awaitability())
                    .addClasses(MyAsyncBeanMethodLevel.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource("project-defaults.yml");
    }

    @Test // Runs on Server
    public void shouldReturnAsyncSum() throws ExecutionException, InterruptedException {
        Integer numberOne = 5;
        Integer numberTwo = 10;

        long start = currentTimeMillis();
        Future<Integer> resultFuture = bean.addNumbers(numberOne, numberTwo);

        assertThat(resultFuture.isDone(), is(equalTo(false)));
        assertThat(currentTimeMillis() - start, is(lessThan(MyAsyncBeanMethodLevel.AWAIT)));

        await().until(() -> resultFuture.isDone());

        assertThat(resultFuture.get(), is(equalTo(numberOne + numberTwo)));
    }

}
