package cn.openwatch.demo.clock_service.model;

import com.google.gson.Gson;

import java.net.URI;
import java.net.URISyntaxException;

import cn.openwatch.demo.clock_service.contract.WebSocketContract;
import cn.openwatch.demo.web_socket.SocketClient;

public class WebSocketModel implements WebSocketContract.Model {

    private SocketClient socketClient;
    private SocketClient.Callback callback;

    public WebSocketModel() {
        super();
    }

    private static String url = "ws://192.168.0.153:8080/websocket/" + "token111";
    @Override
    public void connect(String token, SocketClient.Callback callback) throws URISyntaxException {
        url = "ws://192.168.0.153:8080/websocket/" + token;

        if (token == null || token.isEmpty()) {
            throw new URISyntaxException(url, "token can't be empty");
        }

        try {
            socketClient = new SocketClient(new URI(url), callback);
            socketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        boolean is = socketClient.isOpen();

//        try {
//            socketClient = new SocketClient(new URI(url), callback);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

        socketClient.reconnect();
    }

    public void setCallback(SocketClient.Callback callback) {
        if (socketClient != null) {
            socketClient.setCallback(callback);
        }
    }

    @Override
    public boolean isOpen() {
        return socketClient != null && socketClient.isOpen();
    }

    @Override
    public void send(WebSocketMessage message) {
        if (isOpen()) {
            String data = new Gson().toJson(message);
            
            socketClient.send(data);
        }
    }

    @Override
    public void onDestroy() {
        if (socketClient != null) {
            socketClient.close();
        }
    }
}
