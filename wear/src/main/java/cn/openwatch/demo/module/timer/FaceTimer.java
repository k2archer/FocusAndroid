package cn.openwatch.demo.module.timer;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.wearable.view.WearableDialogHelper;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class FaceTimer {



    public static void showTimePicker(Context context) {
        TimePickerDialog d = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            }
        }, 0, 0, true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }

        d.show();
    }


    private static SparseArray<String> itemMap = new SparseArray<>();

    public static void setItems(String[] items) {
        itemMap.clear();
        for (int i = 0; i < items.length; i++) {
            itemMap.put(i, items[i]);
        }
    }
    public static void showTimerDialog(final Context context, DialogInterface.OnClickListener listener) {
        WearableDialogHelper.DialogBuilder builder = new WearableDialogHelper.DialogBuilder(context);
//        final int[] yourChoice = {-1};
//        AlertDialog.Builder singleChoiceDialog = new AlertDialog.Builder(context);
        builder.setTitle("开始番茄时间");
        // 第二个参数是默认选项，此处设置为0
        String[] items = new String[itemMap.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = itemMap.get(i);
        }
        builder.setSingleChoiceItems(items, 0, listener);
//        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
////                yourChoice[0] = which;
////                Toast.makeText(context, "你选择 " + items[yourChoice[0]], Toast.LENGTH_SHORT).show();
//                long triggerTime = System.currentTimeMillis() + 1000 * (which == 0 ? 5 : 25) * 60;
//                FaceTimer.sendAlarm(context, triggerTime, which);
//
//                dialog.dismiss();
//            }
//        });

        AlertDialog d = builder.create();
//        WindowManager.LayoutParams params = d.getWindow().getAttributes();
//        params.width = 300;
//        params.height = 150;
//        params.alpha = 0.8f;
//        d.getWindow().setAttributes(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }

        d.show();
    }

}
