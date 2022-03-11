package cn.openwatch.demo.module.timer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import cn.openwatch.demo.face.IWatchFace;
import cn.openwatch.demo.utils.NotificationUtils;
import cn.openwatch.demo.utils.VibratorUtils;

public class AlarmClockBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION = "cn.openwatch.demo.receiver.ALARM_CLOCK";


    @Override
    public void onReceive(final Context context, Intent intent) {
        Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show();

        String text = intent.getStringExtra("text");

        // 震动
        VibratorUtils.vibrator(context, 4, 1000);

        // Notification
        Notification.Builder b = NotificationUtils.getNotificationBuilder(context);
        b.setContentTitle(text + "结束");
        b.setContentText("你是最棒的!");
        b.setTimeoutAfter(1000 * 60);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(123, b.build());

        //
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, IWatchFace.class);
        intent.addFlags(0x01000000);
        PendingIntent sender = PendingIntent.getService(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, sender);
    }
}
