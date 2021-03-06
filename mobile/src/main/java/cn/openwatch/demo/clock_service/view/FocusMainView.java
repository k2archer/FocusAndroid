package cn.openwatch.demo.clock_service.view;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.k2archer.demo.common.path.DataPath;
import com.k2archer.demo.common.path.WearDataPackage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.openwatch.communication.ErrorStatus;
import cn.openwatch.communication.OpenWatchCommunication;
import cn.openwatch.communication.OpenWatchSender;
import cn.openwatch.communication.listener.SendListener;
import cn.openwatch.demo.BuildConfig;
import cn.openwatch.demo.R;
import cn.openwatch.demo.clock_service.component.TickingTextView;
import cn.openwatch.demo.clock_service.contract.UserContract;
import cn.openwatch.demo.clock_service.contract.WebSocketContract;
import cn.openwatch.demo.clock_service.model.UserInfo;
import cn.openwatch.demo.clock_service.model.WebSocketMessage;
import cn.openwatch.demo.clock_service.presenter.UserPresenter;
import cn.openwatch.demo.clock_service.presenter.WebSocketPresenter;
import cn.openwatch.demo.web_socket.bo.TickingInfo;
import cn.openwatch.demo.web_socket.dto.WebSocketResponse;
import cn.openwatch.demo.web_socket.dto.response_action.TickingAction;


public class FocusMainView extends BaseView implements UserContract.View, WebSocketContract.View {

    public FocusMainView(Context context) {
        this.context = context;
        create(context);

        init();
    }

    private void create(Context context) {
        rootLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.clock, null);

        initTickingTextView(rootLayout);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams mParams = initWindowParams();
        windowManager.addView(rootLayout, mParams);
    }

    private UserPresenter userPresenter;
    private WebSocketPresenter webSocketPresenter;

    private void init() {
        userPresenter = new UserPresenter(this);

        webSocketPresenter = new WebSocketPresenter(this);

        if (userPresenter.getUserInfo() != null) {
            webSocketPresenter.connect(userPresenter.getUserInfo().getToken());
        }
    }

    private LinearLayout rootLayout;
    private TickingTextView tvTime;

    private WindowManager.LayoutParams initWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        // compatible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        // set bg transparent
        params.format = PixelFormat.RGBA_8888;
        // can not focusable
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.x = 0;
        params.y = 0;
        // window size
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        return params;
    }

    private void initTickingTextView(View rootLayout) {
        tvTime = rootLayout.findViewById(R.id.tv_time);
        tvTime.setCallBack(new TickingTextView.CallBack() {
            @Override
            public void onTickingFinish() {
                // todo ... TickingFinish
                if (webSocketPresenter.getTickingInfo().getType() != TickingInfo.TICKING_TYPE.RESTING) {

                    tvTime.post(new Runnable() {
                        @Override
                        public void run() {
                            tvTime.setText("??????");
                        }
                    });

                }
                if (webSocketPresenter.getTickingInfo().getType() == TickingInfo.TICKING_TYPE.RESTING) {
                    Toast.makeText(tvTime.getContext(), "????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tvTime.isTicking()) {
                    startTicking(31);
                } else {
                    showCancelDialog();
                }
            }
        });
        rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(), "??????", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), TomatoActivity.class);
                v.getContext().startActivity(intent);
                return true;
            }
        });
    }

    int test_count;

    private void startTicking(long time) {

        if (!userPresenter.isLogin()) {
            showLoginDialog();
            return;
        }

        TickingInfo currentTicking = webSocketPresenter.getTickingInfo();

        if (currentTicking != null
                && currentTicking.getType() != TickingInfo.TICKING_TYPE.RESTING
                && currentTicking.getState() == TickingInfo.TickingState.TICKING.getCode()
                && 0 < currentTicking.getEndTime() && currentTicking.getEndTime() < System.currentTimeMillis()) {
            // todo ticking is finish
            showFinishDialog();
            return;
        }

        TickingInfo tickingInfo = new TickingInfo();
        tickingInfo.setAction(TickingAction.START_TICKING);
//        tickingInfo.setTicking(time);
        sendTicking(tickingInfo);
    }

    private void sendTicking(TickingInfo info) {
        WebSocketMessage<TickingInfo> webSocketMessage = new WebSocketMessage<>();
        webSocketMessage.setAction(WebSocketResponse.Action.TICKING);
        webSocketMessage.setData(info);
        webSocketPresenter.send(webSocketMessage);
    }

    private void cancelTicking() {
        TickingInfo tickingInfo = new TickingInfo();
        tickingInfo.setAction(TickingAction.CANCEL_TICKING);
        tickingInfo.setTickingId(webSocketPresenter.getTickingInfo().getTickingId());

        sendTicking(tickingInfo);
    }

    private void showFinishDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_login, null);
        EditText etUserName = (EditText) dialogView.findViewById(R.id.et_username);
        EditText etPassword = (EditText) dialogView.findViewById(R.id.et_password);
        etUserName.setHint("??????");
        etPassword.setHint("????????????");

        dialogBuilder.setTitle("????????????");
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("??????",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etTitle = (EditText) dialogView.findViewById(R.id.et_username);
                        EditText etContent = (EditText) dialogView.findViewById(R.id.et_password);

                        TickingInfo currentTicking = webSocketPresenter.getTickingInfo();
                        currentTicking.setName(etTitle.getText().toString());
                        currentTicking.setEffect(etContent.getText().toString());
                        currentTicking.setAction(TickingAction.FINISH_TICKING);
                        sendTicking(currentTicking);
                    }
                });
        AlertDialog dialog = dialogBuilder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
        } else {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }

        dialog.show();
    }

    private void showCancelDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.create();
        String tickingName = webSocketPresenter.getTickingInfo().getName();
        builder.setTitle("???????????????" + tickingName + "????????????");
        final AlertDialog finalDialog = dialog;
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelTicking();
                finalDialog.dismiss();
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finalDialog.dismiss();
            }
        });
        dialog = builder.create();

//                    dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // 6.0
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
//                        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }

        dialog.show();
    }

    private void showLoginDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_login, null);
        dialogBuilder.setTitle("??????");
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("??????",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etUserName = (EditText) dialogView.findViewById(R.id.et_username);
                        EditText etPassword = (EditText) dialogView.findViewById(R.id.et_password);

                        // todo ????????????
                        if (BuildConfig.DEBUG) {
                            etUserName.setText("kwei");
                            etPassword.setText("123");
                        }

                        userPresenter.login(etUserName.getText().toString(), etPassword.getText().toString());
                        Toast.makeText(context, "?????????", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog dialog = dialogBuilder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
        } else {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && Settings.canDrawOverlays(context)) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                context.startActivity(intent);
            }
        }

        if (checkFloatPermission(context)) {
            dialog.show();
        }

//        int LAYOUT_FLAG;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        } else {
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
//        }
//
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                LAYOUT_FLAG,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);

    }

    public boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }


    @Override
    public void onLogin(UserInfo userInfo) {
        tvTime.setText("??????");
        webSocketPresenter.connect(userInfo.getToken());
    }

    @Override
    public void onStartTicking(TickingInfo tickingInfo) {
        long endTime = tickingInfo.getStartTime() + tickingInfo.getTicking() * 1000;
        tickingInfo.setEndTime(endTime);
        tvTime.startTicking(endTime);

        if (tickingInfo.getAction() == null) {
            tickingInfo.setAction(TickingAction.START_TICKING);
        }
        syncTicking(tickingInfo);
    }

    @Override
    public void onUpdateTicking(TickingInfo info) {
        tvTime.finishTicking();
    }

    @Override
    public void onCancelTicking(TickingInfo tickingInfo) {
        tvTime.cancelTicking();
        syncTicking(tickingInfo);
    }


    // region Wear ??????
    private void syncTicking(TickingInfo tickingInfo) {
        String data = new Gson().toJson(tickingInfo);
        byte[] dataPackage = WearDataPackage.packing(WearDataPackage.PackageCode.TICKING_INFO, data);

        OpenWatchSender.sendData(context, DataPath.SYNC_TICKING, dataPackage, new SendListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(ErrorStatus error) {

                if (checkBluetoothValid()) {
                    Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!OpenWatchCommunication.isConnectedDevice()) {
                    Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(context, "????????????:" + error, Toast.LENGTH_SHORT).show();
            }

            private boolean checkBluetoothValid() {
                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    return false; // ?????????????????????????????????
                }

                return adapter.isEnabled();  // ????????????????????????
            }
        });

    }
    // endregion
}


