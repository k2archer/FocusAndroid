package cn.openwatch.demo.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import cn.openwatch.demo.face.IWatchFace;
import cn.openwatch.demo.module.timer.AlarmClockBroadcastReceiver;

public class AlarmManagerUtils {

    public static void alarm() {
    }

    /**
     * If set, the broadcast will always go to manifest receivers in background (cached
     * or not running) apps, regardless of whether that would be done by default.  By
     * default they will only receive broadcasts if the broadcast has specified an
     * explicit component or package name.
     * <p>
     * NOTE: dumpstate uses this flag numerically, so when its value is changed
     * the broadcast code there must also be changed to match.
     *
     * @hide
     */
    public static final int FLAG_RECEIVER_INCLUDE_BACKGROUND = 0x01000000;


    public static int sendAlarm(Context context, long triggerAtMillis, int type, int requestCode) {
        // 获得系统提供的 AlarmManager 服务的对象
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Intent 设置要启动的组件，这里启动广播
        Intent intent = new Intent(AlarmClockBroadcastReceiver.ACTION);
        intent.addFlags(FLAG_RECEIVER_INCLUDE_BACKGROUND);

        intent.putExtra("text", type == 0 ? "休息时间" : "番茄时间");

        // PendingIntent 对象设置动作, 发送广播!
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 注册闹钟
//        alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, sender);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, sender);

        return requestCode;
    }


    public static final String ALARM_MESSAGE = "alarm_message";


    public static PendingIntent sendAlarmPendingIWatchFace(Context context, int requestCode, Bundle extras, long triggerAtMillis) {
        return sendAlarmPendingService(context, IWatchFace.class, requestCode, extras, triggerAtMillis);
    }

    public static PendingIntent sendAlarmPendingService(Context context, Class<?> cls, int requestCode, Bundle extras, long triggerAtMillis) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, cls);
        intent.addFlags(FLAG_RECEIVER_INCLUDE_BACKGROUND);

        intent.putExtras(extras);

        PendingIntent sender = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, sender);

        return sender;
    }

}
