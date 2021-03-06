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
                    Toast.makeText(DigitalWatchFace.this, "????????? Unpack ??????", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (d.code == WearDataPackage.PackageCode.TICKING_INFO) {
                    Type type = new TypeToken<WearTickingInfo>() {
                    }.getType();
                    mTickingInfo = new Gson().fromJson(d.data, type);
                    if (mTickingInfo == null) {
                        Toast.makeText(DigitalWatchFace.this, "???????????????", Toast.LENGTH_SHORT).show();
                    }
                }

                if (mTickingInfo == null) {
//                    sendNotification("??????", "WearTickingInfo ????????????");
                    return;
                }

                if (mTickingInfo.getAction() == null) {
                    sendNotification("action ??????", d.data);
                    Toast.makeText(DigitalWatchFace.this, "action ??????", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mTickingInfo.getAction().equals(WearTickingInfo.TickingAction.START_TICKING)) {
                    sendNotification("??????", "??????");

//                    preferences.edit().putLong("EndTime", mTickingInfo.getEndTime());
//                    preferences.edit().apply();
                } else if (mTickingInfo.getAction().equals(WearTickingInfo.TickingAction.CANCEL_TICKING)) {
                    sendNotification("??????", "??????");
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
                Toast.makeText(DigitalWatchFace.this, "action ??????", Toast.LENGTH_SHORT).show();
                return;
            }
            if (info.getAction().equals(WearTickingInfo.TickingAction.START_TICKING)) {
                sendNotification("??????", "??????");
//                    preferences.edit().putLong("EndTime", mTickingInfo.getEndTime());
//                    preferences.edit().apply();
            } else if (info.getAction().equals(WearTickingInfo.TickingAction.CANCEL_TICKING)) {
                sendNotification("??????", "??????");
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
                sendNotification("??????", "??????");
                VibratorUtils.vibrator_3(getApplicationContext());
            }
        }, mTickingInfo.getEndTime() - System.currentTimeMillis());
    }


    // ???????????????????????????
    @Override
    public View onCreateView() {
        // TODO Auto-generated method stub

        OpenWatchFaceStyle style = new OpenWatchFaceStyle(this);

        // ?????????????????? ?????????????????? ????????????????????????
        OpenWatchFaceHand secondHand = new OpenWatchFaceHand();
        secondHand.setLength(DisplayUtil.dip2px(this, 10));
        secondHand.setWidth(DisplayUtil.dip2px(this, 3));
//         ???????????????????????????
        secondHand.setDrawGravity(OpenWatchFaceHand.DRAW_GRAVITY_BORDER);

        style.setSecondHand(secondHand);

        // ?????????onTimeUpdate???????????????TIME_UPDATE_PER_SECOND_MODE
        setTimeUpdateMode(TIME_UPDATE_PER_SECOND_MODE);

        // ?????????????????????????????????????????????
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

        // ???????????????????????? ??????????????????
        // //??????????????????
        // getWatchFaceWidth();
        // //??????????????????
        // getWatchFaceHeight();
        // //?????????????????????
        // isInMuteMode();
        // //??????????????????
        // isWatchFaceVisible();
        // //??????????????????
        // getTime();
        // ?????????????????????
         isInPowerSaveMode();

        // ???????????????view???????????????????????????view
        return watchFace;
    }

    // ?????????????????????
    @Override
    public void onWatchFaceCreate() {
        // TODO Auto-generated method stub
        super.onWatchFaceCreate();

        OpenWatchRegister.addDataListener(this, listener);

    }

    // ?????????????????????
    @Override
    public void onWatchFaceDestory() {
        // TODO Auto-generated method stub
        super.onWatchFaceDestory();

        OpenWatchRegister.removeDataListener(listener);
    }

    // ????????????????????????????????????
    @Override
    public void onVisibilityChanged(boolean visible) {
        // TODO Auto-generated method stub
        super.onVisibilityChanged(visible);
    }

    // ??????????????????????????????????????????????????????
    @Override
    public void onPowerSaveModeChanged(boolean inPowerSavedMode) {
        super.onPowerSaveModeChanged(inPowerSavedMode);
    }

    // ?????????????????????????????????
    @Override
    public void onPropertiesChanged(boolean isLowBitAmbient, boolean isBurnInProtection) {
        // TODO Auto-generated method stub
        super.onPropertiesChanged(isLowBitAmbient, isBurnInProtection);
    }

    // ???????????????????????????
    @Override
    public void onTimeZoneChanged() {
        // TODO Auto-generated method stub
        super.onTimeZoneChanged();
    }

    // ?????????????????????????????????
    //
    // ??????????????????
    //
    // ??????????????? ??????????????????????????????????????????????????????
    //
    // ??????????????????
    // timeUpdateMode???TIME_UPDATE_PER_MINUTE_MODE?????? ????????????????????????????????????
    // timeUpdateMode???{@link #TIME_UPDATE_PER_SECOND_MODE?????? ????????????????????????????????????
    //
    @Override
    public void onTimeUpdate(Time time) {
        // TODO Auto-generated method stub
        super.onTimeUpdate(time);

        // ????????????
        invalidate();
    }

    @Override
    public void onWatchFaceDraw(Canvas canvas, Rect bounds) {
        // TODO Auto-generated method stub

        if (mTickingInfo != null && (mTickingInfo.getEndTime() - System.currentTimeMillis()) > 0) {
            Time time = getTime();
            String dateStr = (time.month + 1) + "." + time.monthDay + "  " + (time.hour > 12 ? "??????" : "??????") + " ?????????";
            dateTextView.setText(dateStr);

            long ticking = mTickingInfo.getEndTime() - System.currentTimeMillis();
            timeTextView.setText(StringUtils.formatTime(ticking)); // ????????????????????????
            timeTextView.setTextColor(Color.LTGRAY);

//            if (ticking > 0 && ticking <= 1000) {
//                Toast.makeText(DigitalWatchFace.this, "????????????", Toast.LENGTH_SHORT).show();
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

        String week = time.weekDay == 1 ? "???" : time.weekDay == 2 ? "???" : time.weekDay == 3 ? "???" :
                time.weekDay == 4 ? "???" : time.weekDay == 5 ? "???" : time.weekDay == 6 ? "???" :
                        time.weekDay == 7 ? "???" : "";

        String dateStr = (time.month + 1) + "." + time.monthDay + " " + week + " " + (time.hour > 12 ? "??????" : "??????");
        dateTextView.setText(dateStr);
    }

    private void setTime(Time time) {

        String minStr = time.minute < 10 ? "0" + time.minute : String.valueOf(time.minute);
        String timeStr = (time.hour > 12 ? time.hour - 12 : time.hour) + ":" + minStr;

        timeTextView.setText(timeStr);
        timeTextView.setTextColor(Color.WHITE);
    }



}
