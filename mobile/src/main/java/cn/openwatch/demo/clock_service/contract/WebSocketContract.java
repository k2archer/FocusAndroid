package cn.openwatch.demo.clock_service.contract;

import java.net.URISyntaxException;

import cn.openwatch.demo.clock_service.model.WebSocketMessage;
import cn.openwatch.demo.web_socket.SocketClient;
import cn.openwatch.demo.web_socket.bo.TickingInfo;

public interface WebSocketContract {
    interface Model extends BaseContract.Model {
        void connect(String token, SocketClient.Callback call) throws URISyntaxException;

        void setCallback(SocketClient.Callback callback);

        boolean isOpen();

        void send(WebSocketMessage data);
    }

    interface View extends BaseContract.View {
        void onStartTicking(TickingInfo data);

        void onUpdateTicking(TickingInfo data);

        void onCancelTicking(TickingInfo data);
    }

    interface Presenter extends BaseContract.Presenter {
        void connect(String toekn);

        void send(WebSocketMessage data);
    }
}
