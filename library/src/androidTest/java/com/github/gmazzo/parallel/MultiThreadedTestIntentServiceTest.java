package com.github.gmazzo.parallel;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class MultiThreadedTestIntentServiceTest extends GenericParallelIntentServiceTest {

    @Test
    public void testService() throws TimeoutException, InterruptedException {
        testService(MultiThreadedTestIntentService.class, 200);
    }

}

