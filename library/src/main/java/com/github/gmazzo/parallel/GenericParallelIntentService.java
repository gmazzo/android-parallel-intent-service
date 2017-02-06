package com.github.gmazzo.parallel;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * A generic multithreaded-multiqueued drop-in replacement for {@link android.app.IntentService}.
 * <p>
 * The dispatching logic is the following:
 * # An {@link Intent} is received though {@link #onStart(Intent, int)} or {@link #onStartCommand(Intent, int, int)} methods
 * # {@link #getQueueKeyFor(Intent)} is called to determine the queue {@link K} for the intent
 * # An {@link ExecutorService} is created (if required) for the given queue though {@link #createExecutorFor(Object, Intent)}
 * # A task is dispatched to the executor by calling {@link ExecutorService#submit(Runnable)}
 * # On a working thread, the intent will processed though {@link #onHandleIntent(Intent)}
 *
 * @param <K> The queue type parameter (i.e. {@link Integer} or {@link String}.
 *            <b>It must be compliant with the hash contract between {@link Object#hashCode()} and {@link Object#equals(Object)}</b>
 */
public abstract class GenericParallelIntentService<K> extends Service {
    private final Map<K, ExecutorService> executors = new HashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable onJobFinished = new Runnable() {

        @Override
        public void run() {
            if (--jobsCount <= 0) {
                stopSelf();
            }
        }

    };
    private int jobsCount;

    /**
     * Called to determine the working queue for the given {@link Intent}.
     *
     * @param intent the incoming intent
     * @return an instance of {@link K} (<code>null</code> is a valid value and stands for the default queue)
     */
    @Nullable
    protected abstract K getQueueKeyFor(@NonNull Intent intent);

    /**
     * Called to create an {@link ExecutorService} for the given queue
     *
     * @param queue  the working queue
     * @param intent the incoming intent to be processed (first element of the queue)
     * @return an executor
     */
    @NonNull
    protected abstract ExecutorService createExecutorFor(@Nullable K queue, @NonNull Intent intent);

    /**
     * Same as {@link android.app.IntentService#onHandleIntent(Intent)} but multiple thread can process each given {@link Intent}.
     */
    @WorkerThread
    protected abstract void onHandleIntent(@NonNull Intent intent);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        K key = getQueueKeyFor(intent);

        ExecutorService executor = executors.get(key);
        if (executor == null || executor.isShutdown()) {
            executor = createExecutorFor(key, intent);
            executors.put(key, executor);
        }

        jobsCount++;
        executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    onHandleIntent(intent);

                } catch (RuntimeException throwable) {
                    onTaskFailed(intent, throwable);

                } finally {
                    handler.post(onJobFinished);
                }
            }

        });
    }

    /**
     * Called when a task throws a {@link RuntimeException}.
     * The default implementation will make the application to crash.
     *
     * @param intent    the task intent
     * @param throwable the exception thrown
     */
    @WorkerThread
    protected void onTaskFailed(Intent intent, final RuntimeException throwable) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                throw throwable;
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        int leftTasks = 0;
        for (ExecutorService executor : executors.values()) {
            leftTasks += executor.shutdownNow().size();
        }

        if (leftTasks > 0) {
            throw new IllegalStateException(leftTasks + " tasks were left!");
        }
    }

}
