package com.github.gmazzo.parallel;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class MultiThreadedTestIntentService extends MultiThreadedIntentService {
    private final ServiceTestHelper.SyncBinder syncBinder = new ServiceTestHelper.SyncBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ServiceTestHelper.onHandleIntent(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        syncBinder.onDestroy();
    }

}
