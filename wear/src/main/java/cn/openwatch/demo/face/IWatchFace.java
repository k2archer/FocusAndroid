package cn.openwatch.demo.face;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.wearable.view.WearableDialogHelper;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.k2archer.demo.common.path.DataPackage;
import com.k2archer.demo.common.path.MoblieDataPath;
import com.k2archer.demo.common.path.MoblieDatePackage;
import com.k2archer.demo.common.path.WearTickingInfo;
import com.k2archer.demo.common.utils.StringUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import cn.openwatch.communication.ErrorStatus;
import cn.openwatch.communication.OpenWatchCommunication;
import cn.openwatch.communication.OpenWatchRegister;
import cn.openwatch.communication.OpenWatchSender;
import cn.openwatch.communication.listener.SendListener;
import cn.openwatch.demo.face.element.ViewUtils;
import cn.openwatch.demo.module.timer.FaceTimer;
import cn.openwatch.demo.utils.AlarmManagerUtils;
import cn.openwatch.demo.utils.LocalStorageKVUtils;
import cn.openwatch.demo.utils.NotificationUtils;
import cn.openwatch.demo.R;
import cn.openwatch.demo.common.WearNotificationInfo;
import cn.openwatch.demo.linstener.MobileSyncListener;
import cn.openwatch.demo.utils.VibratorUtils;

import static android.media.AudioManager.ADJUST_RAISE;
import static android.media.AudioManager.STREAM_RING;
import static cn.openwatch.demo.face.FaceSettingActivity.ON_DEBUG_MODE;
import static cn.openwatch.demo.face.FaceSettingActivity.RING_TIME;
import static cn.openwatch.demo.face.FaceSettingActivity.RING_TYPE;
import static cn.openwatch.demo.face.FaceSettingActivity.RING_VIBRATOR_VALUE;
import static cn.openwatch.demo.face.FaceSettingActivity.RING_VOLUME_VALUE;
import static cn.openwatch.demo.utils.AlarmManagerUtils.FLAG_RECEIVER_INCLUDE_BACKGROUND;

public class IWatchFace extends CanvasWatchFaceService {


    public static final String INTENT_KEY_ACTION = "action";
    public static final int ACTION_CODE_RING_START = 2101;
    public static final int ACTION_CODE_RING_STOP = 2102;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && (
                intent.getIntExtra(INTENT_KEY_ACTION, -1) == ACTION_CODE_RING_START
                        || intent.getIntExtra(INTENT_KEY_ACTION, -1) == ACTION_CODE_RING_STOP)) {
            Message msg = new Message();
            msg.what = intent.getIntExtra(INTENT_KEY_ACTION, -1);
            Bundle extra = new Bundle();
            extra.putInt(INTENT_KEY_ACTION, msg.what);
            Log.e("TAG", "onStartCommand:notification:  " + intent.getStringExtra("notification"));
            extra.putString("notification", intent.getStringExtra("notification")); //  判断来自通知打开的跳转
            msg.setData(extra);
            if (engine != null) {
                engine.getEngineHandler().sendMessage(msg);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listener.setNotificationInfoCallback(new MobileSyncListener.NotificationInfo() {
            @Override
            public void onNotify(WearNotificationInfo info) {
                NotificationUtils.sendNotification(IWatchFace.this, info.getTitle(), info.getText());
            }
        });
        OpenWatchRegister.addDataListener(IWatchFace.this, listener);
    }

    private Engine engine;

    @Override
    public Engine onCreateEngine() {
        engine = new Engine();
        return engine;
    }

    private WearTickingInfo wearTickingInfo;
    private MobileSyncListener listener = new MobileSyncListener(new MobileSyncListener.TickingInfoCallback() {
        @Override
        public void onTicking(WearTickingInfo info) {
            wearTickingInfo = info;

            if (wearTickingInfo.getAction().equals(WearTickingInfo.TickingAction.START_TICKING)) {
                long delay = wearTickingInfo.getEndTime() - System.currentTimeMillis();

                engine.startTicking(delay);
//                engine.getEngineHandler().sendEmptyMessageDelayed(EngineHandler.MSG_TICKING_START, delay);

            } else if (wearTickingInfo.getAction().equals(WearTickingInfo.TickingAction.CANCEL_TICKING)) {
                NotificationUtils.sendNotification(IWatchFace.this, "提示", wearTickingInfo.getName() + "取消");
                Bundle data = new Bundle();
                data.putInt(INTENT_KEY_ACTION, ACTION_CODE_RING_STOP);
                engine.stopTickingRing(data);

                engine.cancelTicking();
            }
        }
    });
    private SendListener sendListener = new SendListener() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(ErrorStatus error) {

            if (checkBluetoothValid()) {
                Toast.makeText(IWatchFace.this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!OpenWatchCommunication.isConnectedDevice()) {
                Toast.makeText(IWatchFace.this, "手机未连接", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(IWatchFace.this, "发送失败:" + error, Toast.LENGTH_SHORT).show();
        }

        private boolean checkBluetoothValid() {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                return false; // 你的设备不具备蓝牙功能
            }

            return adapter.isEnabled();  // 蓝牙设备是否打开
        }
    };


    public class Engine extends CanvasWatchFaceService.Engine {

        private View watchFace;
        private TextView timeTextView, dateTextView;
        private Button stopButton;


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(IWatchFace.this).setAcceptsTapEvents(true).build());

            watchFace = View.inflate(IWatchFace.this, R.layout.digital_watchface_layout, null);
            timeTextView = watchFace.findViewById(R.id.watchface_time_tv);
            dateTextView = watchFace.findViewById(R.id.watchface_date_tv);
            stopButton = watchFace.findViewById(R.id.stop_btn);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            OpenWatchRegister.removeDataListener(listener);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

//            /* Check and trigger whether or not tickingRingTimer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            watchFaceWidth = width;
            watchFaceHeight = height;

            rect = new Rect(0, 0, width, height);

            // Measure the view at the exact dimensions (otherwise the text
            // won't center correctly)
            watchFaceWidthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            watchFaceHeightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        }


        private Rect rect;
        private Paint rectPaint = new Paint();
        private int watchFaceWidth, watchFaceHeight;
        private int watchFaceWidthSpec, watchFaceHeightSpec;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Time time = new Time();
            time.setToNow();

            if (wearTickingInfo != null && (wearTickingInfo.getEndTime() - System.currentTimeMillis()) > 0) {
                setDate(time, " 工作中");

                long ticking = wearTickingInfo.getEndTime() - System.currentTimeMillis();
                timeTextView.setText(StringUtils.formatTime(ticking)); // 格式化并更新计时
                timeTextView.setTextColor(Color.LTGRAY);
            } else if (ringStartTime > 0) {
                long t = System.currentTimeMillis() - ringStartTime;
                timeTextView.setText("-" + StringUtils.formatTime(t));
                ViewUtils.showTickingStopFace(watchFace, true);

                setDate(time, " 完成");
            } else {
                setDate(time, "");
                setTime(time);
                stopButton.setVisibility(View.GONE);
            }

            if (watchFace != null) {
                canvas.drawRect(rect, rectPaint);

                watchFace.measure(watchFaceWidthSpec, watchFaceHeightSpec);
                watchFace.layout(0, 0, watchFaceWidth, watchFaceHeight);
                watchFace.draw(canvas);
            }
        }

        private void setDate(Time time, String text) {

            String week = time.weekDay == 1 ? "一" : time.weekDay == 2 ? "二" : time.weekDay == 3 ? "三" :
                    time.weekDay == 4 ? "四" : time.weekDay == 5 ? "五" : time.weekDay == 6 ? "六" :
                            time.weekDay == 0 ? "日" : time.weekDay + "";

            String dateStr = (time.month + 1) + "." + time.monthDay + " " + week + " " + (time.hour > 12 ? "下午" : "上午") + text;
            dateTextView.setText(dateStr);
        }

        private void setTime(Time time) {
            String minStr = time.minute < 10 ? "0" + time.minute : String.valueOf(time.minute);
            String timeStr = (time.hour > 12 ? time.hour - 12 : time.hour) + ":" + minStr;
            timeTextView.setTextColor(Color.WHITE);
            timeTextView.setText(timeStr);
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* Check and trigger whether or not tickingRingTimer should be running (only in active mode). */
            updateTimer();
        }


        // region 更新频率
        private boolean mAmbient;
        /* Handler to update the time once a second in interactive mode. */
        private final Handler engineHandler = new EngineHandler(this);

        public Handler getEngineHandler() {
            return engineHandler;
        }

        /**
         * Starts/stops the {@link #engineHandler} tickingRingTimer based on the state of the watch face.
         */
        private void updateTimer() {
            engineHandler.removeMessages(EngineHandler.MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                engineHandler.sendEmptyMessage(EngineHandler.MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #engineHandler} tickingRingTimer should be running. The tickingRingTimer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();  // 刷新表盘
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                engineHandler.sendEmptyMessageDelayed(EngineHandler.MSG_UPDATE_TIME, delayMs);
            }
        }
        // endregion

        // region 单、双击事件处理

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
//                    Toast.makeText(getApplicationContext(), "TAP_TYPE_TOUCH", Toast.LENGTH_SHORT).show();
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
//                    Toast.makeText(getApplicationContext(), "TAP_TYPE_TOUCH_CANCEL", Toast.LENGTH_SHORT).show();H
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
//                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT).show();
                    clickHandle(tapType, x, y, eventTime);

                    invalidate();
                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }
        }

        private int DOUBLE_CLICK_TIME = 400; // DOUBLE_CLICK_TIME 时间 500ms
        private int DOUBLE_CLICK_AREA = 30;  //  两次点击的点在本区域才算双击
        private long mLastUpInScreenX, mLastUpInScreenY;
        private long mLastClickTime;


        private void clickHandle(int tapType, int x, int y, long eventTime) {
            long mCurrentClickTime = System.currentTimeMillis();

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putInt("tapType", tapType);
            data.putInt("x", x);
            data.putInt("y", y);
            data.putLong("eventTime", eventTime);
            msg.setData(data);

            if (mCurrentClickTime - mLastClickTime < DOUBLE_CLICK_TIME
                    && Math.abs(mLastUpInScreenX - x) < DOUBLE_CLICK_AREA && Math.abs(mLastUpInScreenY - y) < DOUBLE_CLICK_AREA) {
                // 双击事件
                mCurrentClickTime = 0;
                mLastClickTime = 0;

                /// 取消单击，改双击
                engineHandler.removeMessages(EngineHandler.MSG_SINGLE_CLICK_EVENT);
                msg.what = EngineHandler.MSG_DOUBLE_CLICK_EVENT;
                engineHandler.sendMessage(msg);
            } else {
                // 单击事件
                msg.what = EngineHandler.MSG_SINGLE_CLICK_EVENT;
                engineHandler.sendMessageDelayed(msg, DOUBLE_CLICK_TIME + 10);
            }
            mLastUpInScreenX = x;
            mLastUpInScreenY = y;
            mLastClickTime = mCurrentClickTime;
        }

        private void onHandlerClick(int tapType, int x, int y, long eventTime) {
            if (checkIsTouchOnSomeView(x, y, stopButton)) {
                Toast.makeText(IWatchFace.this, "单击 stopButton", Toast.LENGTH_SHORT).show();

                Bundle data = new Bundle();
                data.putInt(INTENT_KEY_ACTION, ACTION_CODE_RING_STOP);
                stopTickingRing(data);
            } else if (checkIsTouchOnSomeView(x, y, timeTextView)) {
                if (wearTickingInfo != null && wearTickingInfo.getEndTime() > System.currentTimeMillis()) {
                    WearableDialogHelper.DialogBuilder builder = new WearableDialogHelper.DialogBuilder(IWatchFace.this);
                    builder.setTitle("提示");
                    builder.setMessage("是否取消");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            cancelTicking();
                            Toast.makeText(IWatchFace.this, "已取消！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    AlertDialog d = builder.create();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    }
                    d.show();
                }

            } else {
                Toast.makeText(IWatchFace.this, "单击", Toast.LENGTH_SHORT).show();
            }
        }


        private boolean checkIsTouchOnSomeView(int x, int y, View view) {
//            Log.e("TAG", "View.Left:" + view.getLeft()
//                    + " View.Right:" + view.getRight()
//                    + " View.Top:" + view.getTop()
//                    + " View.Bottom:" + view.getBottom());
            return view.getRight() - view.getLeft() > 0 &&
                    view.getLeft() <= x && x <= view.getRight()
                    && view.getTop() <= y && y <= view.getBottom();
        }

        private void onHandleDoubleClick(int tapType, int x, int y, long eventTime) {
            Log.e("TAG", "x:" + x + "  y:" + y);
            if (checkIsTouchOnSomeView(x, y, timeTextView)) {
                // todo
                FaceTimer.setItems(new String[]{"5:00", "25:00"});
                boolean isDebugMode = LocalStorageKVUtils.decodeBoolean(ON_DEBUG_MODE, false);
                if (isDebugMode) {
                    FaceTimer.setItems(new String[]{"00:05", "00:25"});
                } else {
                    FaceTimer.setItems(new String[]{"5:00", "25:00"});
                }
                FaceTimer.showTimerDialog(getApplicationContext(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isDebugMode = LocalStorageKVUtils.decodeBoolean(ON_DEBUG_MODE, false);
                        // todo 调试时以 5 秒、25 秒作为番茄时间， 否则以 5 分钟、25 分钟为准
                        int TickingTime = (which == 0 ? 5 : 25) * 1000 * (isDebugMode ? 1 : 60);
                        startTicking(TickingTime);
                        dialog.dismiss();
                    }
                });
            } else if (checkIsTouchOnSomeView(x, y, stopButton)) {
                Toast.makeText(IWatchFace.this, "双击 stopButton", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(IWatchFace.this, "双击", Toast.LENGTH_SHORT).show();
            }
        }
        // endregion

        // region Ticking
        private PendingIntent PendingIntentTicking;

        public void startTicking(long tickingTimeMillis) {
            long triggerTime = System.currentTimeMillis() + tickingTimeMillis;

            Bundle extra = new Bundle();
            extra.putInt(INTENT_KEY_ACTION, ACTION_CODE_RING_START);
            PendingIntentTicking = AlarmManagerUtils.sendAlarmPendingIWatchFace(IWatchFace.this, 0, extra, triggerTime);

            wearTickingInfo = new WearTickingInfo();
            wearTickingInfo.setEndTime(triggerTime);
        }

        public void cancelTicking() {
            if (engine != null && engine.PendingIntentTicking != null) {
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(engine.PendingIntentTicking);
                if (wearTickingInfo != null) {
                    wearTickingInfo.setEndTime(0);
                    if (wearTickingInfo.getTickingId() > 0) {
                        MoblieDatePackage dataPackage = new MoblieDatePackage();
                        dataPackage.data = "";
                        byte[] data = DataPackage.packing(0, "");
                        String path = MoblieDataPath.SYNC_TICKING;
                        OpenWatchSender.sendData(IWatchFace.this, path, data, sendListener);
                    }
                }
            }
        }

        public void tickingFinish() {
//            VibratorUtils.vibrator_3(IWatchFace.this);
//            Bundle data = new Bundle();
//            data.putInt(INTENT_KEY_ACTION, ACTION_CODE_RING_STOP);
//            stopTickingRing(data);
        }
        // endregion

        //region TickingRing

        private long ringStartTime = 0;
//        private long ringMaxTime = LocalStorageKVUtils.decodeInt(RING_TIME, 5); // Ring 最大时间(秒)
        private Vibrator ringVibrator;
        private Ringtone ringRingtone;
        private PendingIntent ringPendingIntent;

        public void startTickingRing(Bundle data) {
            if (data == null || data.getInt(INTENT_KEY_ACTION) != ACTION_CODE_RING_START) {
                return;
            }

            ringStartTime = System.currentTimeMillis();
            // 表盘显示
            ViewUtils.showTickingStopFace(watchFace, true);

            int ringType = LocalStorageKVUtils.decodeInt(RING_TYPE, 0);
            if (ringType == FaceSettingActivity.RingType.SOUND.getCode()
                    || ringType == FaceSettingActivity.RingType.SOUND_VIBRATION.getCode()) {
                // 开启闹钟铃声
                Uri n = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                ringRingtone = RingtoneManager.getRingtone(IWatchFace.this, n);
                // 设置音量
                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int volume = LocalStorageKVUtils.decodeInt(RING_VOLUME_VALUE, 1);
                Log.e("TAG", "startTickingRing: volume: " + volume );
                mAudioManager.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.FLAG_ALLOW_RINGER_MODES);
                ringRingtone.play();
            }
            if (ringType == FaceSettingActivity.RingType.VIBRATION.getCode()
                    || ringType == FaceSettingActivity.RingType.SOUND_VIBRATION.getCode()) {
                // 开启振动
                int vibration = LocalStorageKVUtils.decodeInt(RING_VIBRATOR_VALUE, 3);
                Log.e("TAG", "startTickingRing: vibration: " + vibration );
                ringVibrator = VibratorUtils.vibrator(IWatchFace.this, new long[]{(10 - vibration) * 100, vibration * 100}, 0);
            }

            // 设置最大响铃停止时间
            Bundle extra = new Bundle();
            extra.putInt(INTENT_KEY_ACTION, ACTION_CODE_RING_STOP);
            int ringMaxTime = LocalStorageKVUtils.decodeInt(RING_TIME, 5);
            long triggerTime = System.currentTimeMillis() + 1000 * ringMaxTime; // 停止响铃触发时间
            ringPendingIntent = AlarmManagerUtils.sendAlarmPendingIWatchFace(IWatchFace.this, 0, extra, triggerTime);
            // 开启通知
            Intent intent = new Intent(IWatchFace.this, IWatchFace.class);
            intent.addFlags(FLAG_RECEIVER_INCLUDE_BACKGROUND);
            // 点亮屏幕
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            final PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "Focus:ringLock");
            wakeLock.acquire(1000 * 30); // 亮屏 30 秒
        }

        public void stopTickingRing(Bundle data) {
            if (data == null || data.getInt(INTENT_KEY_ACTION) != ACTION_CODE_RING_STOP) {
                return;
            }

            Log.e("TAG", "stopTickingRing: notification:  " + data.getString("notification"));

            if (data.getString("notification") != null) {
                WearableDialogHelper.DialogBuilder builder = new WearableDialogHelper.DialogBuilder(IWatchFace.this);
                builder.setTitle("提示");
                builder.setMessage("是否结束");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ringStartTime = 0;
                        if (ringVibrator != null) {
                            ringVibrator.cancel();
                        }
                        if (ringRingtone != null) {
                            ringRingtone.stop();
                        }
                        if (ringPendingIntent != null) {
                            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            alarm.cancel(ringPendingIntent);
                        }
                        Toast.makeText(IWatchFace.this, "时间结束，你是最棒的！", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog d = builder.create();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                } else {
                    d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                }
                d.show();
                return;
            }

            ringStartTime = 0;
            if (ringVibrator != null) {
                ringVibrator.cancel();
            }
            if (ringRingtone != null) {
                ringRingtone.stop();
            }
            if (ringPendingIntent != null) {
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(ringPendingIntent);
            }
            Toast.makeText(IWatchFace.this, "时间结束，你是最棒的！", Toast.LENGTH_SHORT).show();
        }
        // endregion
    }

    /*
     * Updates rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);


    private static class EngineHandler extends Handler {

        /**
         * Handler message id for updating the time periodically in interactive mode.
         */
        public static final int MSG_UPDATE_TIME = 0;
        public static final int MSG_SINGLE_CLICK_EVENT = 1000;
        public static final int MSG_DOUBLE_CLICK_EVENT = 1001;
        public static final int MSG_TICKING_FINISH = 1100;
        public static final int MSG_TICKING_START = 1101;

        private final WeakReference<Engine> mWeakReference;

        public EngineHandler(Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                    case MSG_SINGLE_CLICK_EVENT:
                        Bundle data = msg.getData();
                        engine.onHandlerClick(data.getInt("tapType"),
                                data.getInt("x"), data.getInt("y"), data.getLong("eventTime"));
                        break;
                    case MSG_DOUBLE_CLICK_EVENT:
                        data = msg.getData();
                        engine.onHandleDoubleClick(data.getInt("tapType"),
                                data.getInt("x"), data.getInt("y"), data.getLong("eventTime"));
                        break;
                    case MSG_TICKING_FINISH:
                        engine.tickingFinish();
                        break;
                    case MSG_TICKING_START:
//                        engine.startTicking();
                        break;
                    case ACTION_CODE_RING_START:
                        engine.startTickingRing(msg.getData());
                        break;
                    case ACTION_CODE_RING_STOP:
                        engine.stopTickingRing(msg.getData());
                        break;
                }
            }
        }
    }

}
