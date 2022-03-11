package cn.openwatch.demo.utils;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.k2archer.demo.lib_base.utils.LocalStorageKVUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

import cn.openwatch.demo.clock_service.model.UserInfo;
import cn.openwatch.demo.web_socket.bo.TickingInfo;
import cn.openwatch.demo.web_socket.dto.WebSocketResponse;


@RunWith(AndroidJUnit4.class)
public class LocalStorageUtilsTest {

    @Test
    public void test_LocalStorage() {
        Type type = new TypeToken<WebSocketResponse<TickingInfo>>() {
        }.getType();

        String message= "{\"code\":-1,\"msg\":\"{\\\"action\\\":\\\"ticking\\\",\\" +
                "\"data\\\":{\\\"action\\\":\\\"finishTicking\\\",\\\"endTime\\\":1645787139487,\\\"name\\\":\\\"FirstTask\\\",\\\"startTime\\\":1645787108487,\\\"ticking\\\":31,\\\"tickingId\\\":103000000004397,\\\"type\\\":0}} 消息解析错误 \\r\\n### Error updating database.  Cause: java.sql.SQLException: SQL String cannot be empty\\r\\n### SQL: \\r\\n### Cause: java.sql.SQLException: SQL String cannot be empty\\n; SQL String cannot be empty; nested exception is java.sql.SQLException: SQL String cannot be empty\",\"action\":\"\",\"data\":\"\"}";
        WebSocketResponse<TickingInfo> response = new Gson().fromJson(message, type);


        UserInfo user = new UserInfo();
        LocalStorageKVUtils.encode("key", user);

        UserInfo u = LocalStorageKVUtils.decodeParcelable("key", UserInfo.class);
        u.getToken();

    }
}