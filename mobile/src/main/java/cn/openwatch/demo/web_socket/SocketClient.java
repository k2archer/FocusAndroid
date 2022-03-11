package cn.openwatch.demo.web_socket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.ConnectException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketClient extends WebSocketClient {

    public SocketClient(URI serverUri, Callback callback) {
        super(serverUri);
        this.callback = callback;
//        startPing(5000);

    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.e("TAG", "onOpen: " + handshake.getHttpStatus() + " " + handshake.getHttpStatusMessage());

        isReconnecting.set(false);
//        startPing(5000);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("SocketClient:onMessage(): " + message);

        if (callback != null) {
            callback.onMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e("TAG", "onClose: ");
    }

    @Override
    public void onError(Exception ex) {
        Log.e("TAG", "onError: ", ex);
        if (callback != null) {
            callback.onError(ex);
        }
        if (ex instanceof ConnectException) {
            startReconnect();
        }
    }

    private Timer pingTimer;

    class PingTimeer extends Timer {
        @Override
        public void schedule(TimerTask task, long delay) {
            super.schedule(task, delay);
        }
    }

    private void startPing(long period) {
        if (pingTimer != null) {
            pingTimer.cancel();
        }

        pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendPing();
                } catch (WebsocketNotConnectedException ex) {
                    startReconnect();
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }, 0, period);
    }


    private AtomicBoolean isReconnecting = new AtomicBoolean();

    private void startReconnect() {
        try {
            if (!isReconnecting.get() && !isOpen()) {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        reconnect();
                        cancel();
                    }
                }, 5000);

            }
        } catch (Exception e) {
            e.getMessage();
//                e.printStackTrace();
        }
    }

    private Callback callback;

    public interface Callback {
        void onMessage(String message);

        void onError(Exception mes);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
