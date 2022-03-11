package cn.openwatch.demo.clock_service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cn.openwatch.communication.ErrorStatus;
import cn.openwatch.communication.OpenWatchSender;
import cn.openwatch.communication.listener.SendListener;
import cn.openwatch.demo.R;
import cn.openwatch.demo.utils.SoundUtils;
import com.k2archer.demo.common.utils.StringUtils;

import static android.content.Context.POWER_SERVICE;

@Deprecated
public class WatchFaceView {
    private Context context;
    private long workTime = 60 * 25;
    private long restTime = 60 * 5;

    public WatchFaceView(Context context) {
        super();
        this.context = context;
        create(context);
    }

    private LinearLayout clockLayout;
    private Timer timer = new Timer();
    private TextView tvTime;


    private void create(Context context) {

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

        // compatible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        // set bg transparent
        mParams.format = PixelFormat.RGBA_8888;
        // can not focusable
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.x = 0;
        mParams.y = 0;
        // window size
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        mParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        // get layout
//        LayoutInflater inflater = LayoutInflater.from(this);
        clockLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.clock, null);
        tvTime = clockLayout.findViewById(R.id.tv_time);
        clockLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvTime.getText().toString().equals("开始")) {
//                    onStartTicking(60 * 25, "任务");
                    startTickingEx(workTime, "任务");
                } else if (tvTime.getText().toString().equals("完成")) {
//                    onStartTicking(60 * 5, "休息");
                    startTickingEx(restTime, "休息");

                    Toast toast = Toast.makeText(tvTime.getContext(), "休息开始", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, -200, 0);
                    toast.show();

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(tvTime.getContext());
                    AlertDialog dialog = builder.create();
                    builder.setTitle("你确定取消任务吗？");
                    final AlertDialog finalDialog = dialog;
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            timer.cancel();
                            tvTime.setText("开始");
                            finalDialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finalDialog.dismiss();
                        }
                    });
                    dialog = builder.create();

//                    dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // 6.0
                        dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
//                        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    }

                    dialog.show();
                }
            }
        });


        windowManager.addView(clockLayout, mParams);

        soundUtils.initSoundPool(context);


    }

    private void startTickingEx(final long countdown, final String message) {

        PowerManager powerManager = (PowerManager) clockLayout.getContext().getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.acquire();


        clockLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + countdown * 1000;

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        clockLayout.post(new TickingTask(message, startTime, endTime));
                        soundUtils.playSound(clockLayout.getContext(), 1, 0);
                    }
                }, System.currentTimeMillis() % 1000, 1000);
            }
        }, 0);
    }

    private void finishTicking() {
        PowerManager powerManager = (PowerManager) clockLayout.getContext().getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.release();
    }

    private SoundUtils soundUtils = new SoundUtils();

    class TickingTask extends TimerTask {
        private long startTime, endTime;
        private String message;


        public TickingTask(String message, long startTime, long endTime) {
            super();
            this.message = message;
            this.startTime = startTime;
            this.endTime = endTime;

        }

        @Override
        public void run() {
            final TextView tv = clockLayout.findViewById(R.id.tv_time);
            long ticking = (endTime - System.currentTimeMillis());
            if (ticking > 0) {
                tv.setText(StringUtils.formatTime(ticking));
            } else {
                timer.cancel();
                String wearNotification = "";
                if (message.equals("任务")) {
                    tv.setText("完成");
                    wearNotification = "任务完成";
                    soundUtils.playSound(clockLayout.getContext(), 2, 0);
                }
                if (message.equals("休息")) {
                    tv.setText("开始");
                    wearNotification = "休息结束";
                    soundUtils.playSound(clockLayout.getContext(), 2, 0);
                }

                OpenWatchSender.sendData(context, "/ticking", wearNotification, new SendListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(ErrorStatus error) {

                    }
                });

                Toast toast = Toast.makeText(tv.getContext(), wearNotification, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }


}

