package cn.openwatch.demo.face;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WearableDialogHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.openwatch.demo.R;
import cn.openwatch.demo.utils.LocalStorageKVUtils;

public class FaceSettingActivity extends AppCompatActivity {

    public static final String ON_DEBUG_MODE = "on_debug_mode";
    public static final String RING_TIME = "ring_time";
    public static final String RING_TYPE = "ring_type";
    public static final String RING_VOLUME_VALUE = "ring_volume_value";
    public static final String RING_VIBRATOR_VALUE = "ring_vibrator_value";


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
//                WearableDialogHelper.DialogBuilder b = new WearableDialogHelper.DialogBuilder(FaceSettingActivity.this);
                final android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(FaceSettingActivity.this);

                b.setTitle("选择响铃类型");
                final View view = LayoutInflater.from(FaceSettingActivity.this).inflate(R.layout.dialog_ring_setting, null);
                b.setView(view);
                final android.app.AlertDialog d = b.create();

                final TextView tvRingVolume = view.findViewById(R.id.ring_volume_value_tv);
                int volume  = LocalStorageKVUtils.decodeInt(RING_VOLUME_VALUE, 1);
                tvRingVolume.setText("音量: " + volume);
                SeekBar sbVolume = view.findViewById(R.id.ring_volume_sb);
                sbVolume.setProgress(volume);
                sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        tvRingVolume.setText("音量: " + progress);
                        LocalStorageKVUtils.encodeInt(RING_VOLUME_VALUE, progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                final TextView tvRingVibrator = view.findViewById(R.id.ring_vibration_value_tv);
                int vibration= LocalStorageKVUtils.decodeInt(RING_VIBRATOR_VALUE, 1);
                tvRingVibrator.setText("震动: " +vibration );
                SeekBar sbVibrator = view.findViewById(R.id.ring_vibration_sb);
                sbVibrator.setProgress(vibration);
                sbVibrator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        tvRingVibrator.setText("震动: " + progress);
                        LocalStorageKVUtils.encodeInt(RING_VIBRATOR_VALUE, progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                RadioGroup radioGroup = view.findViewById(R.id.type_rg);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
//                String[] items = new String[]{"响铃", "震动", "震动和响铃"};
                        switch (checkedId) {
                            case R.id.rb_0:
                                LocalStorageKVUtils.encodeInt(RING_TYPE, 0);
                                btnRingType.setText("响铃类型: " + RingType.getString(RingType.codeOf(0)));
                                break;
                            case R.id.rb_1:
                                LocalStorageKVUtils.encodeInt(RING_TYPE, 1);
                                btnRingType.setText("响铃类型: " + RingType.getString(RingType.codeOf(1)));
                                break;
                            case R.id.rb_2:
                                LocalStorageKVUtils.encodeInt(RING_TYPE, 2);
                                btnRingType.setText("响铃类型: " + RingType.getString(RingType.codeOf(2)));
                                break;
                        }
                        d.dismiss();
                    }
                });

                d.show();
            }
        });


    }
}
