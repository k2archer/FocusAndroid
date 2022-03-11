package cn.openwatch.demo.face;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.k2archer.demo.common.path.DataPackage;
import com.k2archer.demo.common.path.DataPath;
import com.k2archer.demo.common.path.WearDataPackage;
import com.k2archer.demo.common.path.WearTickingInfo;
import com.k2archer.demo.common.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.Timer;
import java.util.TimerTask;

import cn.openwatch.communication.DataMap;
import cn.openwatch.communication.OpenWatchRegister;
import cn.openwatch.communication.listener.DataListener;
import cn.openwatch.demo.R;
import cn.openwatch.demo.linstener.MobileSyncListener;
import cn.openwatch.demo.utils.DisplayUtil;
import cn.openwatch.demo.utils.NotificationUtils;
import cn.openwatch.demo.utils.VibratorUtils;
import cn.openwatch.watchface.OpenWatchFace;
import cn.openwatch.watchface.OpenWatchFaceHand;
import cn.openwatch.watchface.OpenWatchFaceStyle;

public class DigitalWatchFace extends OpenWatchFace {


    @Override
    public Engine onCreateEngine() {
        initTickingInfo();
//        return super.onCreateEngine();
        Engine engine = new Engine();
        engine.setTouchEventsEnabled(true);
        return  engine;
    }

    private class Engine extends OpenWatchFace.WatchFaceEngine {

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            watchFace.dispatchTouchEvent(event);

//            float x = event.getX();
//            float y = event.getY();
//            if (timeTextView.getLeft() < x && x < timeTextView.getRight()
//            && timeTextView.getTop() < y && y < timeTextView.getBottom()) {
//                Toast.makeText(DigitalWatchFace.this, "touchEvent", Toast.LENGTH_SHORT).show();
//            }
        }
    }


    private SharedPreferences preferences;

    private void initTickingInfo() {
        preferences = getSharedPreferences("ticking_info", MODE_PRIVATE);
        if (mTickingInfo == null) {
            mTickingInfo = new WearTickingInfo();
        }
        mTickingInfo.setEndTime(preferences.getLong("endTime", 0));
    }

    private WearTickingInfo mTickingInfo;
    private View watchFace;
    private TextView timeTextView, dateTextView;
    private DataListener listener = new DataListener() {
        @Override
        public void onDataMapReceived(String path, DataMap dataMap) {

        }

        @Override
        public void onDataReceived(String path, byte[] rawData) {
            if (path.equals(DataPath.SYNC_TICKING)) {

                DataPackage d = DataPackage.unpack(rawData);
                if (d == null) {
                    Toast.makeText(DigitalWatchFace.this, "数据包 Unpack 失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (d.code == WearDataPackage.PackageCode.TICKING_INFO) {
                    Type type = new TypeToken<WearTickingInfo>() {
                    }.getType();
                    mTickingInfo = new Gson().fromJson(d.data, type);
                    if (mTickingInfo == null) {
                        Toast.makeText(DigitalWatchFace.this, "数据包为空", Toast.LENGTH_SHORT).show();
                    }
                }

                if (mTickingInfo == null) {
//                    sendNotification("提示", "WearTickingInfo 解析错误");
                    return;
                }

                if (mTickingInfo.getAction() == null) {
                    sendNotification("action 为空", d.data);
                    Toast.makeText(DigitalWatchFace.this, "action 为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mTickingInfo.getAction().equals(WearTickingInfo.TickingAction.START_TICKING)) {
                    sendNotification("提示", "开始");

//                    preferences.edit().putLong("EndTime", mTickingInfo.getEndTime());
//                    preferences.edit().apply();
                } else if (mTickingInfo.getAction().equals(WearTickingInfo.TickingAction.CANCEL_TICKING)) {
                    sendNotification("提示", "取消");
//                    VibratorUtils.vibrator_3(getApplicationContext());
                }
            }

        }

        @Override
        public void onDataDeleted(String path) {

        }
    };


    MobileSyncListener.TickingInfoCallback callback = new MobileSyncListener.TickingInfoCallback() {
        @Override
        public void onTicking(WearTickingInfo info) {
            if (info.getAction() == null) {
                Toast.makeText(DigitalWatchFace.this, "action 为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (info.getAction().equals(WearTickingInfo.TickingAction.START_TICKING)) {
                sendNotification("提示", "开始");
//                    preferences.edit().putLong("EndTime", mTickingInfo.getEndTime());
//                    preferences.edit().apply();
            } else if (info.getAction().equals(WearTickingInfo.TickingAction.CANCEL_TICKING)) {
                sendNotification("提示", "取消");
//                    VibratorUtils.vibrator_3(getApplicationContext());

                if (tickingFinishTimer != null) {
                    tickingFinishTimer.cancel();
                }
            }

            mTickingInfo = info;
        }
    };

    private void sendNotification(final String title, final String text) {
        Notification.Builder b = NotificationUtils.getNotificationBuilder(watchFace.getContext());
        b.setContentText(text);
        b.setContentTitle(title);

        NotificationManager mNotificationManager = (NotificationManager) watchFace.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, b.build());

        Toast.makeText(DigitalWatchFace.this, text, Toast.LENGTH_SHORT).show();
    }


    private Timer tickingFinishTimer;

    private void starTickingFinishTimer() {
        tickingFinishTimer = new Timer();
        tickingFinishTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendNotification("提示", "完成");
                VibratorUtils.vibrator_3(getApplicationContext());
            }
        }, mTickingInfo.getEndTime() - System.currentTimeMillis());
    }


    // 构建表盘布局时回调
    @Override
    public View onCreateView() {
        // TODO Auto-generated method stub

        OpenWatchFaceStyle style = new OpenWatchFaceStyle(this);

        // 如果需要的话 构建表盘秒针 否则不会绘制秒针
        OpenWatchFaceHand secondHand = new OpenWatchFaceHand();
        secondHand.setLength(DisplayUtil.dip2px(this, 10));
        secondHand.setWidth(DisplayUtil.dip2px(this, 3));
//         秒针在表盘边界显示
        secondHand.setDrawGravity(OpenWatchFaceHand.DRAW_GRAVITY_BORDER);

        style.setSecondHand(secondHand);

        // 并设置onTimeUpdate回调频率为TIME_UPDATE_PER_SECOND_MODE
        setTimeUpdateMode(TIME_UPDATE_PER_SECOND_MODE);

        // 表盘上的通知卡片以单行高度显示
        style.setCardPeekMode(OpenWatchFaceStyle.PEEK_MODE_SHORT);


        setStyle(style);

        watchFace = View.inflate(this, R.layout.digital_watchface_layout, null);
        timeTextView = (TextView) watchFace.findViewById(R.id.watchface_time_tv);
        dateTextView = (TextView) watchFace.findViewById(R.id.watchface_date_tv);

        watchFace.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(DigitalWatchFace.this, "timeTextView Touch", Toast.LENGTH_SHORT).show();
                dateTextView.setTextColor(Color.GREEN);
                DigitalWatchFace.this.invalidate();
                return true;
            }
        });

        // 可获取的相关属性 详见相关文档
        // //获取表盘宽度
        // getWatchFaceWidth();
        // //获取表盘高度
        // getWatchFaceHeight();
        // //是否为静音模式
        // isInMuteMode();
        // //表盘是否可见
        // isWatchFaceVisible();
        // //获取当前时间
        // getTime();
        // 是否为省电模式
         isInPowerSaveMode();

        // 返回自定义view或者布局文件生成的view
        return watchFace;
    }

    // 表盘创建时回调
    @Override
    public void onWatchFaceCreate() {
        // TODO Auto-generated method stub
        super.onWatchFaceCreate();

        OpenWatchRegister.addDataListener(this, listener);

    }

    // 表盘销毁时回调
    @Override
    public void onWatchFaceDestory() {
        // TODO Auto-generated method stub
        super.onWatchFaceDestory();

        OpenWatchRegister.removeDataListener(listener);
    }

    // 表盘可见性发生变化时回调
    @Override
    public void onVisibilityChanged(boolean visible) {
        // TODO Auto-generated method stub
        super.onVisibilityChanged(visible);
    }

    // 表盘从省电模式切换到非省电模式时回调
    @Override
    public void onPowerSaveModeChanged(boolean inPowerSavedMode) {
        super.onPowerSaveModeChanged(inPowerSavedMode);
    }

    // 表盘属性发生变化时回调
    @Override
    public void onPropertiesChanged(boolean isLowBitAmbient, boolean isBurnInProtection) {
        // TODO Auto-generated method stub
        super.onPropertiesChanged(isLowBitAmbient, isBurnInProtection);
    }

    // 时区发生改变时回调
    @Override
    public void onTimeZoneChanged() {
        // TODO Auto-generated method stub
        super.onTimeZoneChanged();
    }

    // 表盘不可见时：不会回调
    //
    // 表盘可见时：
    //
    // 省电模式下 当分钟时间、时区、日期发生变化时回调
    //
    // 非省电模式下
    // timeUpdateMode为TIME_UPDATE_PER_MINUTE_MODE时， 当分钟时间发生变化时回调
    // timeUpdateMode为{@link #TIME_UPDATE_PER_SECOND_MODE时， 当秒钟时间发生变化时回调
    //
    @Override
    public void onTimeUpdate(Time time) {
        // TODO Auto-generated method stub
        super.onTimeUpdate(time);

        // 刷新表盘
        invalidate();
    }

    @Override
    public void onWatchFaceDraw(Canvas canvas, Rect bounds) {
        // TODO Auto-generated method stub

        if (mTickingInfo != null && (mTickingInfo.getEndTime() - System.currentTimeMillis()) > 0) {
            Time time = getTime();
            String dateStr = (time.month + 1) + "." + time.monthDay + "  " + (time.hour > 12 ? "下午" : "上午") + " 工作中";
            dateTextView.setText(dateStr);

            long ticking = mTickingInfo.getEndTime() - System.currentTimeMillis();
            timeTextView.setText(StringUtils.formatTime(ticking)); // 格式化并更新计时
            timeTextView.setTextColor(Color.LTGRAY);

//            if (ticking > 0 && ticking <= 1000) {
//                Toast.makeText(DigitalWatchFace.this, "任务完成", Toast.LENGTH_SHORT).show();
//                VibratorUtils.vibrator_3(getApplicationContext());
//            }

        } else {
            Time time = getTime();
            setDate(time);
            setTime(time);
        }

        super.onWatchFaceDraw(canvas, bounds);
    }

    private void setDate(Time time) {

        String week = time.weekDay == 1 ? "一" : time.weekDay == 2 ? "二" : time.weekDay == 3 ? "三" :
                time.weekDay == 4 ? "四" : time.weekDay == 5 ? "五" : time.weekDay == 6 ? "六" :
                        time.weekDay == 7 ? "日" : "";

        String dateStr = (time.month + 1) + "." + time.monthDay + " " + week + " " + (time.hour > 12 ? "下午" : "上午");
        dateTextView.setText(dateStr);
    }

    private void setTime(Time time) {

        String minStr = time.minute < 10 ? "0" + time.minute : String.valueOf(time.minute);
        String timeStr = (time.hour > 12 ? time.hour - 12 : time.hour) + ":" + minStr;

        timeTextView.setText(timeStr);
        timeTextView.setTextColor(Color.WHITE);
    }



}
