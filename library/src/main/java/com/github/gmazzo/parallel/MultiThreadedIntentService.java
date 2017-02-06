package com.github.gmazzo.parallel;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A single queued multithreaded drop-in replacement for {@link android.app.IntentService}.
 * <p>
 * With the default implementation, each incoming {@link Intent} will be executed on a new thread.
 * You may override {@link #createExecutorFor(Void, Intent)} to provide a customized
 * {@link ExecutorService} for example, limiting the number the {@link Thread}s, i. e.:
 * <pre>
 * &#64;NonNull
 * &#64;Override
 * protected ExecutorService createExecutorFor(@Nullable Void queue, @NonNull Intent intent) {
 *     return Executors.newFixedThreadPool(10);
 * }
 * </pre>
 * <p>
 * <i>This implementation is best suitable for unlimited background processing.</i>
 */
public abstract class MultiThreadedIntentService extends GenericParallelIntentService<Void> {

    @Nullable
    @Override
    protected final Void getQueueKeyFor(@NonNull Intent intent) {
        return null;
    }

    @NonNull
    @Override
    protected ExecutorService createExecutorFor(@Nullable Void queue, @NonNull Intent intent) {
        return Executors.newCachedThreadPool();
    }

}
