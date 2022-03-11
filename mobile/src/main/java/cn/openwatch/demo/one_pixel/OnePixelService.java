package cn.openwatch.demo.one_pixel;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class OnePixelService extends Service {
    OnePixelManager manager;

    public OnePixelService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = new OnePixelManager();
        manager.registerOnePixelReceiver(this); // 注册广播接收者
        return START_STICKY; // 返回 START_STICKY，保证服务被杀死后可以被重新创建
    }

    @Override
    public void onDestroy() {
        manager.unregisterOnePixelReceiver(this);
    }

    public static void startService(Context context){
        Intent intent = new Intent();
        intent.setClass(context, OnePixelService.class);
        context.startService(intent);
    }
}