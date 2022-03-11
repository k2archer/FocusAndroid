package cn.openwatch.demo.face.element;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.openwatch.demo.R;

public class ViewUtils {

    public static void showTickingStopFace(View watchFace, boolean isShow) {
        TextView timeTextView = watchFace.findViewById(R.id.watchface_time_tv);
        Button stopButton = watchFace.findViewById(R.id.stop_btn);

        if (isShow) {
            timeTextView.setTextColor(Color.RED);
            stopButton.setVisibility(View.VISIBLE);
        } else {
            timeTextView.setTextColor(Color.WHITE);
            stopButton.setVisibility(View.GONE);
        }
    }

}
