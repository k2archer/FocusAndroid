package cn.openwatch.demo.one_pixel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class OnePixelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TAG", "onReceive: " );
        String action = intent.getAction();
        OnePixelManager manager = new OnePixelManager();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            manager.finishOnePixelActivity();
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            manager.startOnePixelActivity(context);
        }
    }
}
