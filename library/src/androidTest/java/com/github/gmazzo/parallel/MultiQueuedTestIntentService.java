package com.github.gmazzo.parallel;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MultiQueuedTestIntentService extends MultiQueuedIntentService {
    private static final int MAX_QUEUES = 5;
    private final ServiceTestHelper.SyncBinder syncBinder = new ServiceTestHelper.SyncBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncBinder;
    }

    @Nullable
    @Override
    protected String getQueueKeyFor(@NonNull Intent intent) {
        int id = intent.getIntExtra("id", 0);
        return String.valueOf("queue#" + (id % MAX_QUEUES));
    }

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        ServiceTestHelper.onHandleIntent(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        syncBinder.onDestroy();
    }

}
