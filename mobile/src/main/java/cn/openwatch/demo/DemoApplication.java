package cn.openwatch.demo;

import android.app.Application;
import android.content.Context;

import com.k2archer.demo.lib_base.utils.LocalStorageKVUtils;

import cn.openwatch.communication.OpenWatchCommunication;


public class DemoApplication extends Application {


    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        context = getApplicationContext();

//        GlobalCrashHandler.getInstance().init(this, BuildConfig.DEBUG);

        LocalStorageKVUtils.init(getApplicationContext());

        // 手机端初始化
        OpenWatchCommunication.init(this);

    }

}
