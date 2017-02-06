package com.github.gmazzo.parallel;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.util.SparseIntArray;

import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@MediumTest
@RunWith(AndroidJUnit4.class)
public abstract class GenericParallelIntentServiceTest {
    private final String logTag = getClass().getName();

    protected void testService(Class<? extends Service> serviceClass, int jobsCount) throws TimeoutException, InterruptedException {
        Context context = InstrumentationRegistry.getTargetContext();
        Handler handler = new Handler(Looper.getMainLooper());
        final Semaphore sync = new Semaphore(0);
        ServiceConnection conn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ((ServiceTestHelper.SyncBinder) service).sync = sync;
                sync.release();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

        };

        context.bindService(new Intent(context, serviceClass), conn, Context.BIND_AUTO_CREATE);

        final SparseIntArray results = new SparseIntArray(jobsCount);
        for (int i = 0; i < jobsCount; i++) {
            final int jobId = i;
            ResultReceiver receiver = new ResultReceiver(handler) {

                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    synchronized (results) {
                        results.put(jobId, resultCode);

                        Log.i(logTag, "Job finished: total=" + results.size());
                    }
                }

            };

            Log.i(logTag, "Dispatching job: service=" + serviceClass + ", jobId=" + jobId);
            Intent intent = ServiceTestHelper.makeIntent(context, serviceClass, jobId, receiver);
            context.startService(intent);
        }

        sync.acquire(); // waits for bound
        context.unbindService(conn); // frees the service, to let it work as an IntentService

        Log.i(logTag, "Awaiting on all jobs to be processed...");
        sync.acquire(); // waits for destroy

        Log.i(logTag, "Finished");

        assertEquals("Jobs count not the same!", jobsCount, results.size());
    }

}

final class ServiceTestHelper {
    private static final String LOG_TAG = ServiceTestHelper.class.getName();
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_RESULT = "result";
    private static final Random RANDOM = new Random();

    static Intent makeIntent(Context context, Class<? extends Service> serviceClass, int id, ResultReceiver receiver) {
        return new Intent(context, serviceClass)
                .putExtra(EXTRA_ID, id)
                .putExtra(EXTRA_RESULT, receiver);
    }

    static void onHandleIntent(Intent intent) {
        String threadName = Thread.currentThread().getName();
        int id = intent.getIntExtra(EXTRA_ID, -1);
        ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RESULT);

        Log.i(LOG_TAG, threadName + " start work: id=" + id);

        long start = System.currentTimeMillis();
        try {
            long wait = Math.max(Math.round(RANDOM.nextGaussian() * 400 + 200), 20);
            Thread.sleep(wait);

        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "failed!", e);

        } finally {
            long took = System.currentTimeMillis() - start;

            Log.i(LOG_TAG, threadName + " finished: id=" + id + ", took=" + took);

            receiver.send(id, null);
        }
    }

    static class SyncBinder extends Binder {
        Semaphore sync;

        void onDestroy() {
            sync.release();
        }

    }

}
