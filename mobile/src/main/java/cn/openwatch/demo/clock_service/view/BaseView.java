package cn.openwatch.demo.clock_service.view;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import cn.openwatch.demo.clock_service.contract.BaseContract;

public class BaseView implements BaseContract.View {

    protected Context context;

    @Override
    public void showToast(final String message) {

        try {
            Log.w("TAG", "showToast: " + message);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }.start();
        }

    }
}
