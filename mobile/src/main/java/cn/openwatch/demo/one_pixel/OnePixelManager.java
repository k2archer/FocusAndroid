package cn.openwatch.demo.one_pixel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

public class OnePixelManager {

    private WeakReference<Activity> mActivity;
    private OnePixelReceiver onePixelReceiver;

    public void registerOnePixelReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        onePixelReceiver = new OnePixelReceiver();
        context.registerReceiver(onePixelReceiver, filter);
    }

    public void unregisterOnePixelReceiver(Context context) {
        if (onePixelReceiver != null) {
            context.unregisterReceiver(onePixelReceiver);
        }
    }

    public void startOnePixelActivity(Context context){
        Intent intent = new Intent();
        intent.setClass(context,OnePixelActivity.class);
        context.startActivity(intent);
    }

    public void finishOnePixelActivity() {
        if (mActivity != null) {
            Activity activity = mActivity.get();
            if (activity != null) {
                activity.finish();
            }
            mActivity = null;
        }
    }

    public void setKeepAliveReference(OnePixelActivity activity){
        mActivity = new WeakReference<Activity>(activity);
    }
}
