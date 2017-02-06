package com.github.gmazzo.parallel;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class MultiQueuedTestIntentServiceTest extends GenericParallelIntentServiceTest {

    @Test
    public void testService() throws TimeoutException, InterruptedException {
        testService(MultiQueuedTestIntentService.class, 200);
    }

}
