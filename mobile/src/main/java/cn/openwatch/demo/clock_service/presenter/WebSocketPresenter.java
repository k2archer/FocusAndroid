package cn.openwatch.demo.clock_service.presenter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.k2archer.demo.lib_base.utils.LocalStorageKVUtils;
import com.k2archer.lib_network.retrofit.response.ResponseStateCode;

import java.lang.reflect.Type;
import java.net.URISyntaxException;

import cn.openwatch.demo.BuildConfig;
import cn.openwatch.demo.clock_service.common.KeyValueConstant;
import cn.openwatch.demo.clock_service.contract.WebSocketContract;
import cn.openwatch.demo.clock_service.model.WebSocketMessage;
import cn.openwatch.demo.clock_service.model.WebSocketModel;
import cn.openwatch.demo.web_socket.SocketClient;
import cn.openwatch.demo.web_socket.bo.TickingInfo;
import cn.openwatch.demo.web_socket.dto.response_action.TickingAction;
import cn.openwatch.demo.web_socket.dto.WebSocketResponse;

public class WebSocketPresenter extends BasePresenter implements WebSocketContract.Presenter {


    public WebSocketPresenter(WebSocketContract.View view) {
        BasePresenter(view, new WebSocketModel());
    }

    @Override
    public void connect(String token) {

        try {
            ((WebSocketModel) model).connect(token, call);
        } catch (URISyntaxException e) {
            mView.showToast("连接服务器失败 " + (BuildConfig.DEBUG ? e.getMessage() : ""));
            e.printStackTrace();
        }

    }

    private SocketClient.Callback call = new SocketClient.Callback() {
        @Override
        public void onMessage(String message) {
            Type type = new TypeToken<WebSocketResponse<TickingInfo>>() {
            }.getType();

            WebSocketResponse<TickingInfo> response = null;
            try {
                response = new Gson().fromJson(message, type);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (response == null) {
                mView.showToast("转换失败 webSocket message: " + message);
                return;
            }
            if (response.getCode() == ResponseStateCode.BAD_TOKEN.getCode()) {
                LocalStorageKVUtils.remove(KeyValueConstant.USER_INFO);
                mView.showToast(message + " 请重新登录");
                return;
            }
            if (response.getAction().equals(WebSocketResponse.Action.TICKING)) {
                handleTicking(response.getData());
            }
        }

        @Override
        public void onError(Exception ex) {
            mView.showToast(ex.getMessage());
//            if (ex instanceof ConnectException) {
//                try {
//                    ((WebSocketModel) model).connect();
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    };


    private TickingInfo tickingInfo;

    public TickingInfo getTickingInfo() {
        return tickingInfo;
    }

    private void handleTicking(TickingInfo info) {
        tickingInfo = info;
        if (info.getAction().equals(TickingAction.START_TICKING)) {
            ((WebSocketContract.View) mView).onStartTicking(info);
        } else if (info.getAction().equals(TickingAction.UPDATE_TICKING)) {
            ((WebSocketContract.View) mView).onUpdateTicking(info);
        } else if (info.getAction().equals(TickingAction.CANCEL_TICKING)) {
            ((WebSocketContract.View) mView).onCancelTicking(info);
        }
    }

    @Override
    public void send(WebSocketMessage data) {
        if (((WebSocketModel) model).isOpen()) {
            try {
                ((WebSocketModel) model).send(data);
            } catch (Exception e) {
                mView.showToast("发送失败");
            }
        } else {
            mView.showToast("服务器未连接");

            ((WebSocketModel) model).reconnect();
        }

    }

    public boolean isConnect() {
        return ((WebSocketModel) model).isOpen();
    }
}
