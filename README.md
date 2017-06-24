# android-parallel-intent-service
A multithreaded IntentService which can process requests in parallel and potentially keep multiple working queues.

## Import
On your `build.gradle` add:
```
    dependencies {
        compile 'com.github.gmazzo:parallel-intent-service:0.4'
    }
```

## Usage
```java
public class SampleService extends MultiThreadedIntentService {

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO do something here
    }

}
```
And in your `AndroidManifest.xml` add:
```xml
    <service android:name=".SampleService" />
```

## Provide a custom Executor
```java
public class SampleService extends MultiThreadedIntentService {

    @NonNull
    @Override
    protected ExecutorService createExecutorFor(@Nullable Void queue, @Nullable Intent intent) {
        return Executors.newFixedThreadPool(10);
    }

}
```

## Multiple queues sample
In this example, we have defined two abstract queues: default `null` and `"myQueue"`.
The queue comes as an extra in the `Intent`, but that's completly up to the user.
```java
public class SampleService extends MultiQueuedIntentService {

    @Nullable
    @Override
    protected final String getQueueKeyFor(@Nullable Intent intent) {
        return intent.getStringExtra("queue");
    }

    @NonNull
    @Override
    protected ExecutorService createExecutorFor(@Nullable String queue, @Nullable Intent intent) {
        if ("myQueue".equals(queue)) {
            return Executors.newFixedThreadPool(10);
        }
        return Executors.newSingleThreadExecutor();
    }

}
```
