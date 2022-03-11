package cn.openwatch.demo;

import android.app.Application;

import cn.openwatch.communication.OpenWatchCommunication;
import cn.openwatch.demo.exception.GlobalCrashHandler;
import cn.openwatch.demo.utils.LocalStorageKVUtils;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        LocalStorageKVUtils.init(this);

//        GlobalCrashHandler.getInstance().init(this, BuildConfig.DEBUG);

        // 手表端初始化
        OpenWatchCommunication.init(this);

    }
}
