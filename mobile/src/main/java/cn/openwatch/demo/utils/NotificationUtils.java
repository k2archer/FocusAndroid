package cn.openwatch.demo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import cn.openwatch.demo.R;

import static android.app.Notification.VISIBILITY_SECRET;

//import androidx.core.app.NotificationCompat;

public class NotificationUtils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    public static Notification.Builder getNotificationBuilder(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name",
                    NotificationManager.IMPORTANCE_DEFAULT);
            //是否绕过请勿打扰模式
            channel.canBypassDnd();
            //闪光灯
            channel.enableLights(true);
            //锁屏显示通知
            channel.setLockscreenVisibility(VISIBILITY_SECRET);
            //闪关灯的灯光颜色
            channel.setLightColor(Color.RED);
            //桌面launcher的消息角标
            channel.canShowBadge();
            //是否允许震动
            channel.enableVibration(true);
            //获取系统通知响铃声音的配置
            channel.getAudioAttributes();
            //获取通知取到组
            channel.getGroup();
            //设置可绕过  请勿打扰模式
            channel.setBypassDnd(true);
            //设置震动模式
            channel.setVibrationPattern(new long[]{100, 100, 200});
            //是否会有灯光
            channel.shouldShowLights();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
        }
        Notification.Builder notification = new Notification.Builder(context, "channel_id");
        notification.setContentTitle("新消息来了");
        notification.setContentText("周末到了，不用上班了");
        notification.setSmallIcon(R.drawable.ic_launcher);
        notification.setAutoCancel(true);
        return notification;
    }


    public static void sendNotification(Context context, String title, String text) {
        Notification.Builder b = NotificationUtils.getNotificationBuilder(context);
        b.setContentText(text);
        b.setContentTitle(title);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, b.build());

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
