package com.fiberfox.fxt.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.fiberfox.fxt.R;


public class ForegroundService extends Service {

    Context context;

    public ForegroundService(Context context) {
        this.context = context;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String strId = "foreground service";
            final String strTitle = getString(R.string.app_name);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(strId);
            if (channel == null) {
                channel = new NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_NONE);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat
                    .Builder(this, strId)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Ideally, this method would simply return START_NOT_STICKY and the service wouldn't be
        // restarted automatically. Unfortunately, this seems to not be the case as the log is filled
        // with messages from BluetoothCommunicator and MainService after a crash when this method
        // returns START_NOT_STICKY. The following does seem to work.
        if (intent == null) {
            Log.e("logdinobei", "Service was stopped and automatically restarted by the system. Stopping self now.");
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
