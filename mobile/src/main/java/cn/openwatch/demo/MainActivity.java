package cn.openwatch.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.k2archer.demo.common.path.DataPackage;
import com.k2archer.demo.common.path.MoblieDataPath;
import com.k2archer.demo.common.path.WearDataPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.openwatch.communication.BothWayCallback;
import cn.openwatch.communication.DataMap;
import cn.openwatch.communication.ErrorStatus;
import cn.openwatch.communication.OpenWatchBothWay;
import cn.openwatch.communication.OpenWatchRegister;
import cn.openwatch.communication.OpenWatchSender;
import cn.openwatch.communication.SpecialData;
import cn.openwatch.communication.listener.ConnectListener;
import cn.openwatch.communication.listener.DataListener;
import cn.openwatch.communication.listener.MessageListener;
import cn.openwatch.communication.listener.SendListener;
import cn.openwatch.communication.listener.SpecialTypeListener;
import cn.openwatch.demo.clock_service.ClockService;

import static android.app.Notification.VISIBILITY_SECRET;

// 手表端代码逻辑基本亦同
public class MainActivity extends Activity
        implements OnClickListener, ConnectListener, DataListener, MessageListener, SpecialTypeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.send_data_btn).setOnClickListener(this);
        findViewById(R.id.send_bitmap_data_btn).setOnClickListener(this);
        findViewById(R.id.send_datamap_data_btn).setOnClickListener(this);
        findViewById(R.id.send_stream_data_btn).setOnClickListener(this);

        findViewById(R.id.send_msg_btn).setOnClickListener(this);
        findViewById(R.id.send_bitmap_msg_btn).setOnClickListener(this);
        findViewById(R.id.send_datamap_msg_btn).setOnClickListener(this);
        findViewById(R.id.send_stream_msg_btn).setOnClickListener(this);

        findViewById(R.id.send_bothway).setOnClickListener(this);

//        findViewById(R.id.check_wear_app_update).setOnClickListener(this);


//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                setPermission();
//            }
//        });

        if (!isIgnoringBatteryOptimizations()) {
            requestIgnoreBatteryOptimizations();
        }
        hideAppWindow(this, true);
    }

    //在多任务列表页面隐藏App窗口

    void hideAppWindow(Context context, Boolean isHide) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            //控制App的窗口是否在多任务列表显示
            activityManager.getAppTasks().get(0).setExcludeFromRecents(isHide);
//            activityManager.appTasks[0].setExcludeFromRecents(isHide);
        } catch (Exception e) {
//            .....
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int OVERLAY_PERMISSION_REQ_CODE = 123;

    private void setPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                //有悬浮窗权限开启服务绑定 绑定权限
                Intent intent = new Intent(MainActivity.this, ClockService.class);
                startService(intent);

            } else {
                //没有悬浮窗权限m,去开启悬浮窗权限
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            //默认有悬浮窗权限  但是 华为, 小米,oppo等手机会有自己的一套Android6.0以下  会有自己的一套悬浮窗权限管理 也需要做适配
            Intent intent = new Intent(MainActivity.this, ClockService.class);
            startService(intent);
        }


        // 手动悬浮权限
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//        intent.setData(Uri.fromParts("package", getPackageName(), null));
//        startActivity(intent);


//                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 0);
//                startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1);
//
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                startActivityForResult(intent, FLAT_REQUEST_CODE);
//
//                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//                startActivityForResult(accessibleIntent, ACCESSIBILITY_REQUEST_CODE);

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        // 根据业务需求
        // 添加设备连接的监听
        OpenWatchRegister.addConnectListener(this);
        // 添加接收数据的监听
        OpenWatchRegister.addDataListener(this);
        OpenWatchRegister.addMessageListener(this);
        // 添加接收图片、文件、数据流等特殊类型数据的监听
        OpenWatchRegister.addSpecialTypeListener(this);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        // 清除这个界面注册的所有监听
        OpenWatchRegister.removeDataListener(this);
        OpenWatchRegister.removeConnectListener(this);
        OpenWatchRegister.removeMessageListener(this);
        OpenWatchRegister.removeSpecialDataListener(this);
    }

    private void sendData() {
        // 当配对设备未连接 数据并不会被丢失 会在下次连接上配对设备时接收到数据
        // 发送基础数据
        OpenWatchSender.sendData(this, "/send_data", "你好 openwatch", new MySendListener("data"));
    }

    private void sendBitmapData() {
        // 发送图片
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        OpenWatchSender.sendData(this, "/send_bitmap", bitmap, new MySendListener("图片"));
    }

    private void sendDatamapData() {
        // 发送键值对
        DataMap datamap = new DataMap();
        datamap.putString("key", "value");
        OpenWatchSender.sendData(this, "/send_datamap", datamap, new MySendListener("键值对"));
    }

    private void sendFileOrStreamData() {
        try {
            InputStream is = getAssets().open("test.txt");
            OpenWatchSender.sendData(this, "/send_stream", is, new MySendListener("数据流"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // // 发送文件
        // OpenWatchSender.sendData(this, "/send_file", new
        // File(filePath), new MySendListener("文件"));
    }

    private void sendMsg() {
        // 当配对设备未连接 数据会被丢失 用于发送临时性或时效性数据
        // 发送基础数据
        OpenWatchSender.sendMsg(this, "/send_msg", "你好 openwatch", new MySendListener("msg"));
    }

    private void sendBitmapMsg() {
        // 发送图片
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        OpenWatchSender.sendMsg(this, "/send_bitmap", bitmap2, new MySendListener("图片"));
    }

    private void sendDatamapMsg() {
        // 发送键值对
        DataMap datamap2 = new DataMap();
        datamap2.putString("key", "value");
        OpenWatchSender.sendMsg(this, "/send_datamap", datamap2, new MySendListener("键值对"));
    }

    private void sendFileOrStreamMsg() {
        try {
            InputStream is = getAssets().open("test.txt");
            OpenWatchSender.sendMsg(this, "/send_stream", is, new MySendListener("数据流"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // // 发送文件
        // OpenWatchSender.sendMsg(this, "/send_file", new
        // File(filePath), new MySendListener("文件"));
    }

    private void sendBothWay() {
        OpenWatchBothWay.request(this, "/send_bothway", "你好 openwatch", new BothWayCallback() {

            @Override
            public void onResponsed(byte[] rawData) {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "手表端响应数据:" + new String(rawData), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(ErrorStatus error) {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "数据请求错误" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.send_data_btn:
                sendData();
                break;
            case R.id.send_bitmap_data_btn:
                setPermission();

//                sendBitmapData();
                break;
            case R.id.send_datamap_data_btn:
//                notifyEx();
//                sendNotification();
//                getNotificationManager().notify(1, getNotificationBuilder().packing());
//                sendDatamapData();
                break;
            case R.id.send_stream_data_btn:
                sendFileOrStreamData();
                break;
            case R.id.send_msg_btn:
                sendMsg();
                break;
            case R.id.send_bitmap_msg_btn:
                sendBitmapMsg();
                break;
            case R.id.send_datamap_msg_btn:
                sendDatamapMsg();
                break;
            case R.id.send_stream_msg_btn:
                sendFileOrStreamMsg();
                break;
            case R.id.send_bothway:
                sendBothWay();
                break;

            default:
                break;
        }
    }

    private NotificationManager mNotificationManager;

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    @NonNull
    private NotificationCompat.Builder getNotificationBuilder() {
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
            getNotificationManager().createNotificationChannel(channel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "channel_id");
        notification.setContentTitle("新消息来了");
        notification.setContentText("周末到了，不用上班了");
        notification.setSmallIcon(R.drawable.ic_launcher);
        notification.setAutoCancel(true);
        return notification;
    }

    private void sendNotification() {
        Log.e("TAG", "sendNotification: ");
        //获取NotificationManager实例
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.drawable.ic_launcher)
                //设置通知标题
                .setContentTitle("最简单的Notification")
                //设置通知内容
                .setContentText("只有小图标、标题、内容");
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //.setWhen(System.currentTimeMillis());
        //通过builder.packing()方法生成Notification对象,并发送通知,id=1
        notifyManager.notify(1, builder.build());
    }

    private void notifyEx() {


        Bitmap btm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                MainActivity.this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("5 new message")
                .setContentText("twain@android.com");
        mBuilder.setTicker("New message");//第一次提示消息的时候显示在通知栏上
        mBuilder.setNumber(12);
        mBuilder.setLargeIcon(btm);
        mBuilder.setAutoCancel(true);//自己维护通知的消失

        //构建一个Intent
        Intent resultIntent = new Intent(MainActivity.this,
                MainActivity.class);
        //封装一个Intent
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                MainActivity.this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // 设置通知主题的意图
        mBuilder.setContentIntent(resultPendingIntent);
        //获取通知管理器对象
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

//        int notificationId = 001;
//        // The channel ID of the notification.
//        String id = "my_channel_01";
//        // Build intent for notification content
//        Intent viewIntent = new Intent(this, MainActivity.class);
//        viewIntent.putExtra("EXTRA_EVENT_ID", 1231);
//        PendingIntent viewPendingIntent =
//                PendingIntent.getActivity(this, 0, viewIntent, 0);
//
//        // Notification channel ID is ignored for Android 7.1.1
//        // (API level 25) and lower.
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, id)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle("tilte")
//                        .setContentText("text")
//                        .setContentIntent(viewPendingIntent);
//
//        // Get an instance of the NotificationManager service
//        NotificationManagerCompat notificationManager =
//                NotificationManagerCompat.from(this);
//
//        // Issue the notification with notification manager.
//        notificationManager.notify(notificationId, notificationBuilder.packing());
    }


    private class MySendListener implements SendListener {

        private String tag;

        private MySendListener(String tag) {
            this.tag = tag;
        }

        @Override
        public void onSuccess() {
            // TODO Auto-generated method stub
            Toast.makeText(MainActivity.this, "发送" + tag + "成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(ErrorStatus error) {
            // TODO Auto-generated method stub
            Toast.makeText(MainActivity.this, "发送" + tag + "失败 原因是:" + error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMessageReceived(String path, byte[] rawData) {
        // TODO Auto-generated method stub
        if (path.equals("/send_bothway")) {
            // 接收到手表端请求数据并响应 必须传入接收到的path
            OpenWatchBothWay.response(this, path, "response bothway");
        } else {
            Toast.makeText(this, getClass().getSimpleName() + ":手表发来临时性数据:" + new String(rawData), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onDataReceived(String path, byte[] rawData) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：手表发来数据:" + new String(rawData), Toast.LENGTH_SHORT).show();

        if (path.equals(MoblieDataPath.SYNC_TICKING)) {
            DataPackage data = WearDataPackage.unpack(rawData);
        }
    }

    @Override
    public void onDataDeleted(String path) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：手表删除了一条数据", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataMapReceived(String path, DataMap dataMap) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：手表发来键值对", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBitmapReceived(String path, Bitmap bitmap) {
        // TODO Auto-generated method stub
        ImageView imageView = (ImageView) findViewById(R.id.received_bitmap);
        imageView.setImageBitmap(bitmap);
        Toast.makeText(this, getClass().getSimpleName() + "：手表端发来图片", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileReceived(SpecialData data) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：手表端发来文件：" + new String(data.getData()), Toast.LENGTH_SHORT)
                .show();

        //保存成文件
        data.receiveFile(getExternalCacheDir() + File.separator + "file.txt");
    }

    @Override
    public void onStreamReceived(SpecialData data) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：手表端发来数据流：" + new String(data.getData()), Toast.LENGTH_SHORT).show();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getExternalCacheDir(), "file.txt"));
            //写入到输出流
            data.receiveStream(fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onInputClosed(String path) {
        //在调用SpecialData的receiveStream或receiveFile后回调
        Toast.makeText(this, getClass().getSimpleName() + "：保存手表端发来的数据完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeerConnected(String displayName, String nodeId) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：和手表连接上了  设备名：" + displayName + " 设备id：" + nodeId,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeerDisconnected(String displayName, String nodeId) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：和手表断开了连接  设备名：" + displayName + " 设备id：" + nodeId,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceConnectionSuspended(int cause) {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：和连接服务意外断开了", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceConnected() {
        // TODO Auto-generated method stub
        Toast.makeText(this, getClass().getSimpleName() + "：已连接上连接服务", Toast.LENGTH_SHORT).show();
    }
}
