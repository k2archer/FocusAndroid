package cn.openwatch.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmSoundBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_STRING = "[mzx].[clwang].action.[ACTION_STRING]";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show();
        Log.e("TAG", "onReceive: " + "11111111111111111" );
    }
}
