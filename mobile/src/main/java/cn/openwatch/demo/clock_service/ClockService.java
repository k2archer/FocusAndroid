package cn.openwatch.demo.clock_service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.NotificationCompat;

import cn.openwatch.demo.MainActivity;
import cn.openwatch.demo.R;
import cn.openwatch.demo.clock_service.view.FocusMainView;
import cn.openwatch.demo.utils.NotificationUtils;
import cn.openwatch.internal.basic.utils.LogUtils;

public class ClockService extends AccessibilityService {

    private FocusMainView mainView;

    @Override
    public void onCreate() {
        super.onCreate();

        initMediaPlayer();

        mainView = new FocusMainView(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    private int notificationId = 11;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Notification.Builder b = NotificationUtils.getNotificationBuilder(this);
//        Notification n = b.build();
//        startForeground(notificationId , n);
        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        pausePlaySong();

        // 重启服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(getApplicationContext(), ClockService.class));
        } else {
//            startForegroundService(new Intent(getApplicationContext(), ClockService.class));
            startService(new Intent(getApplicationContext(), ClockService.class));
        }
    }


    //<editor-fold desc="无声保活">
    /* -------------------------------------- 无声保活 Start ---------------------------------------*/
    private MediaPlayer mMediaPlayer;

    private void initMediaPlayer() {
        LogUtils.setEnableLog(true);
        LogUtils.d("播放：");

        MediaPlayerThread playerThread = new MediaPlayerThread();
        thread = new Thread(playerThread);
        mMediaPlayer = MediaPlayer.create(this, R.raw.no_kill);
        mMediaPlayer.setLooping(true);
        thread.start();
        LogUtils.d("播放时 线程名称：" + thread.getName());
    }

    //开始、暂停播放
    private void startPlaySong() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.no_kill);
            LogUtils.d("音乐启动播放,播放对象为： " + mMediaPlayer.hashCode());
            mMediaPlayer.start();
        } else {
            mMediaPlayer.start();
            LogUtils.d("音乐启动播放,播放对象为： " + mMediaPlayer.hashCode());
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            LogUtils.d("音乐启动播放,播放对象为： " + mMediaPlayer.hashCode());
            int progress = mMediaPlayer.getCurrentPosition();
            LogUtils.d("音乐暂停，播放进度：" + progress);
        }
    }

    //停止播放销毁对象
    private void stopPlaySong() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            LogUtils.d("音乐停止播放,播放对象为：" + mMediaPlayer.hashCode());
            LogUtils.d("音乐播放器是否在循环：" + mMediaPlayer.isLooping());
            LogUtils.d("音乐播放器是否还在播放：" + mMediaPlayer.isPlaying());
            mMediaPlayer.release();
            LogUtils.d("播放对象销毁,播放对象为：" + mMediaPlayer.hashCode());
            mMediaPlayer = null;
        }
    }

    private void pausePlaySong() {
        mMediaPlayer.pause();
        LogUtils.d("恢复播放 时当前播放器对象：" + mMediaPlayer.hashCode());
        stopPlaySong();
        LogUtils.d("应用播放服务被杀死，正在重启");
        LogUtils.d("目标播放工作线程是否存活：" + thread.isAlive());
    }


    private Thread thread;


    class MediaPlayerThread implements Runnable {

        @Override
        public void run() {
            startPlaySong();
        }
    }
//</editor-fold>
    /* -------------------------------------- 无声保活 End ---------------------------------------*/
}
