package cn.openwatch.demo.clock_service.component;

import android.content.Context;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import cn.openwatch.demo.utils.SoundUtils;

import com.k2archer.demo.common.utils.StringUtils;

import cn.openwatch.demo.web_socket.bo.TickingInfo;

import static android.content.Context.POWER_SERVICE;

public class TickingTextView extends TextView {

    private SoundUtils soundUtils = new SoundUtils();

    public TickingTextView(Context context) {
        super(context);
    }

    public TickingTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TickingTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        soundUtils.initSoundPool(getContext());
    }


    private long endTime;

    public long getEndTime() {
        return endTime;
    }

    public void startTicking(long endTime) {
        this.endTime = endTime;
        startTimer();
    }
    public void cancelTicking() {
        post(new Runnable() {
            @Override
            public void run() {
                setText("开始");
            }
        });
        endTime = 0;
        cancelTimer();
    }

    public void finishTicking() {
        cancelTicking();
    }
    public boolean isTicking() {
        return timer != null;
    }


    private Timer timer;

    //region Timer
    private void startTimer() {
        if (timer != null) {
            return;
        }

        acquireWakeLock();

        timer = new Timer();
        long delay = endTime % 1000;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TickingTextView.this.post(new TickingTask()); // 更新 Text
                // 播放滴答声音
                soundUtils.playSound(TickingTextView.this.getContext(), 1, 0);
            }
        }, delay, 1000);
    }

    class TickingTask extends TimerTask {

        @Override
        public void run() {
            long ticking = (endTime - System.currentTimeMillis());
            if (ticking > 0) {
                TickingTextView.this.setText(StringUtils.formatTime(ticking)); // 格式化并更新计时
            } else {
                cancelTicking();

                if (callBack != null) {
                    callBack.onTickingFinish();
                }
            }
        }
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        releaseWakeLock();
    }

    private CallBack callBack;

    public interface CallBack {

        void onTickingFinish();

    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }
    //endregion

    //region WakeLock
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) this.getContext().getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FocusApp::TickingWakelockTag");
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        PowerManager powerManager = (PowerManager) this.getContext().getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FocusApp::TickingWakelockTag");
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
    //endregion
}
