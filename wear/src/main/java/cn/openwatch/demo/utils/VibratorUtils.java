package cn.openwatch.demo.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Vibrator;

import java.util.Timer;
import java.util.TimerTask;

public class VibratorUtils {


    public static Vibrator vibrator(Context context, long[] pattern, int repeat) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//        long[] pattern = {500, 1000, 500, 1000};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, repeat);
        return vibrator;
    }

    public static void vibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }

    public static void vibrator(final Context context, final int time, int periodAtMillis) {
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            private int count = 0;

            @Override
            public void run() {
                if (count < time) {
                    vibrator(context);
                } else {
                    timer.cancel();
                }
                count++;
            }
        };
        timer.schedule(task, 0, periodAtMillis);
    }

    public static void vibrator_3(final Context context) {

        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            private int count = 0;

            @Override
            public void run() {
                if (count < 3) {
                    vibrator(context);
                } else {
                    timer.cancel();
                }
                count++;
            }
        };

        timer.schedule(task, 0, 1000);
    }
}
