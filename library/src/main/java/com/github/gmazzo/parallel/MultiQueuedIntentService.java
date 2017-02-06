package com.github.gmazzo.parallel;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A multiple queued multi-threaded drop-in replacement for {@link android.app.IntentService}.
 * <p>
 * With the default implementation, each incoming {@link Intent} dispached to one queue
 * trough {@link #getQueueKeyFor(Intent)}. Each queue will have its dedicated working {@link Thread}.
 * The number of thread is tied to the number of different queues final implementation defines.
 * <p>
 * <i>This implementation is best suitable prioritizing tasks</i>, creating a dedicated queue for some sepecific job for example.
 */
public abstract class MultiQueuedIntentService extends GenericParallelIntentService<String> {

    @NonNull
    @Override
    protected ExecutorService createExecutorFor(@Nullable String queue, @NonNull Intent intent) {
        return Executors.newSingleThreadExecutor();
    }

}
