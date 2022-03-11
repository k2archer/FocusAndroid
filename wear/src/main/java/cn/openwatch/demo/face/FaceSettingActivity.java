package cn.openwatch.demo.face;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.wearable.view.WearableDialogHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.openwatch.demo.R;
import cn.openwatch.demo.utils.LocalStorageKVUtils;

public class FaceSettingActivity extends AppCompatActivity {

    public static final String ON_DEBUG_MODE = "on_debug_mode";
    public static final String RING_TIME = "ring_time";
    public static final String RING_TYPE = "ring_type";

    enum RingType {
        VIBRATION(0),
        SOUND(1),
        SOUND_VIBRATION(2);

        final int code;

        RingType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static String getString(RingType state) {
            for (RingType stateCode : values()) {
                if (state == SOUND) {
                    return "响铃";
                } else if (state == VIBRATION) {
                    return "震动";
                } else if (state == SOUND_VIBRATION) {
                    return "震动|响铃";
                }
            }
            throw new RuntimeException("没有找到对应的状态值");
        }

        public static RingType codeOf(int code) {
            for (RingType stateCode : values()) {
                if (stateCode.getCode() == code) {
                    return stateCode;
                }
            }
            throw new RuntimeException("没有找到对应的响应状态");
        }

        public static int indexCode(RingType state) {
            for (RingType stateCode : values()) {
                if (stateCode.code == state.code) {
                    return state.code;
                }
            }
            throw new RuntimeException("没有找到对应的状态值");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_setting);

        initView();
    }

    public void initView() {
        Button btnDebug = findViewById(R.id.tv_is_debug_setting);
        boolean isDebugMode = LocalStorageKVUtils.decodeBoolean(ON_DEBUG_MODE, false);
        btnDebug.setText("调试模式(" + (isDebugMode ? "开" : "关") + ")");
        btnDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDebugMode = LocalStorageKVUtils.decodeBoolean(ON_DEBUG_MODE, false);
                LocalStorageKVUtils.encodeBoolean(ON_DEBUG_MODE, !isDebugMode);

                isDebugMode = LocalStorageKVUtils.decodeBoolean(ON_DEBUG_MODE, false);
                ((Button) v).setText("调试模式(" + (isDebugMode ? "开" : "关") + ")");
            }
        });

        final Button tvRingTime = findViewById(R.id.tv_ring_time_setting);
        int ringTime = LocalStorageKVUtils.decodeInt(RING_TIME, 0);
        tvRingTime.setText("结束提示时间：" + ringTime + " 秒");
        tvRingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(FaceSettingActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setSingleLine();
                editText.setHint("响铃时间(秒)");
                editText.setGravity(Gravity.CENTER);
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(FaceSettingActivity.this);
                inputDialog.setTitle("输入时间").setView(editText);
                inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = editText.getText().toString();
                        if (value.length() == 0) {
                            return;
                        }
                        int time = Integer.valueOf(value);
                        LocalStorageKVUtils.encodeInt(RING_TIME, time);
                        tvRingTime.setText("结束提示时间：" + time + " 秒");
                    }
                }).show();

            }
        });

        final Button btnRingType = findViewById(R.id.tv_ring_type_setting);

        int ringType = LocalStorageKVUtils.decodeInt(RING_TYPE, 0);
        btnRingType.setText("响铃类型: " + RingType.getString(RingType.codeOf(ringType)));
        btnRingType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WearableDialogHelper.DialogBuilder b = new WearableDialogHelper.DialogBuilder(FaceSettingActivity.this);
                b.setTitle("选择响铃类型");

                String[] items = new String[]{"响铃", "震动", "震动和响铃"};
                b.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalStorageKVUtils.encodeInt(RING_TYPE, which);
                        btnRingType.setText("响铃类型: " + RingType.getString(RingType.codeOf(which)));
                        dialog.dismiss();
                    }
                });
                b.create().show();
            }
        });
    }
}
