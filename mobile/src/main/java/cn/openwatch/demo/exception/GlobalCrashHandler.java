package cn.openwatch.demo.exception;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.openwatch.demo.BuildConfig;

public class GlobalCrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    // 实现单例 CrashHandler
    private GlobalCrashHandler() { }
    public static GlobalCrashHandler getInstance() {
        return SingletonHolder.sInstance;
    }
    private static class SingletonHolder {
        private static final GlobalCrashHandler sInstance = new GlobalCrashHandler();
    }

    // 初始化函数
    public void init(Context context, boolean isDefaultHandler) {
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
        if (isDefaultHandler) {
            mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable throwable) {

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "App 已崩溃", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();

        try {
            dumpExceptionToSDCard(throwable); // 写入文件存储到本地或同时上传至网络保存
        } catch (IOException e) {
            e.printStackTrace();
            if (BuildConfig.DEBUG) {
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(mContext, "日志保存失败", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }.start();
            }

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());   // 结束进程
                }
            }, 5 * 1000);
            return;
        }

        throwable.printStackTrace();

        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(t, throwable);
        } else {
//            restartApp(mContext, LoginActivity.class, 2000);              // 重启 App
            android.os.Process.killProcess(android.os.Process.myPid());   // 结束进程
        }
    }

    private void dumpExceptionToSDCard(Throwable throwable) throws IOException {
        // 检查外部存储是否可用
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        // todo .. 检查外部存储读写权限
        String[] checkList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        List<String> needRequestList = checkPermission(mContext, checkList);
        if (!needRequestList.isEmpty()) {
            if (BuildConfig.DEBUG) {
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(mContext, "无存储读写权限", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }.start();
            }
            return;
        }

        // 拼接 Crash info 保存文件路径
        String PACKAGE_NAME = mContext.getPackageName();
        String appName = PACKAGE_NAME.substring(PACKAGE_NAME.lastIndexOf('.') + 1);
        String CRASH_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
                + "/com.k2archer/" + appName + "/log/";

        File directory = new File(CRASH_FILE_PATH);
        if (!directory.exists())
            directory.mkdirs();

        Date date = new Date(System.currentTimeMillis());
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        File file = new File(CRASH_FILE_PATH + "renter_crash" + time + ".txt");

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        writer.println(time);  // exception crash time

        try {
            getPhoneInfo(writer);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        writer.println();
        throwable.printStackTrace(writer);
        writer.close();

        // 刷新 MTP 缓存
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        mContext.sendBroadcast(intent);
    }


    private List<String> checkPermission(Context context, String[] checkList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < checkList.length; i++) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, checkList[i])) {
                list.add(checkList[i]);
            }
        }
        return list;
    }

    private void getPhoneInfo(PrintWriter writer) throws PackageManager.NameNotFoundException {
        PackageManager manager = mContext.getPackageManager();
        PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        String appVersion = "App Version: " + info.versionName + "_" + info.versionCode;
        writer.println(appVersion);

        String OSVersion =
                "OS Version: " + Build.VERSION.RELEASE + "_" + Build.VERSION.SDK;
        writer.println(OSVersion);

        // 手机的制造商、型号、架构
        writer.println("Vendor:" + Build.MANUFACTURER);
        writer.println("Model: " + Build.MODEL);
        writer.println("CPU ARI: " + Build.CPU_ABI);
    }
}