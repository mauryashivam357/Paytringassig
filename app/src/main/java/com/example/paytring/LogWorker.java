package com.example.paytring;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LogWorker extends Worker {

    public LogWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    @NonNull
    @Override
    public Result doWork() {
        long timestamp = System.currentTimeMillis();
        Log.d("LogWorker", "Background job executed at " + timestamp);
        return Result.success();
    }
}
