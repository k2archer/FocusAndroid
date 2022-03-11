package cn.openwatch.demo.linstener;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.k2archer.demo.common.path.DataPackage;
import com.k2archer.demo.common.path.DataPath;
import com.k2archer.demo.common.path.WearDataPackage;
import com.k2archer.demo.common.path.WearTickingInfo;

import java.lang.reflect.Type;

import cn.openwatch.communication.DataMap;
import cn.openwatch.communication.listener.DataListener;
import cn.openwatch.demo.common.WearNotificationInfo;

public class MobileSyncListener implements DataListener {

    private MobileSyncListener() {

    }

    public MobileSyncListener(TickingInfoCallback callback) {
        this.tickingInfoCallback = callback;
    }

    @Override
    public void onDataMapReceived(String path, DataMap dataMap) {

    }

    @Override
    public void onDataReceived(String path, byte[] rawData) {
        if (path.equals(DataPath.SYNC_TICKING)) {
            DataPackage d = DataPackage.unpack(rawData);

            if (d == null) {
                return;
            }

            if (d.code == WearDataPackage.PackageCode.TICKING_INFO) {
                onTicking(d.data);
            } else if (d.code == WearDataPackage.PackageCode.NOTIFICATION_INFO) {
                onNotification(d.data);
            }


        }
    }

    @Override
    public void onDataDeleted(String path) {

    }

    private interface BaseCall {
    }

    // region Ticking
    private TickingInfoCallback tickingInfoCallback;

    public interface TickingInfoCallback extends BaseCall {
        void onTicking(WearTickingInfo info);
    }

    private void onTicking(String data) {
        Type type = new TypeToken<WearTickingInfo>() {
        }.getType();
        WearTickingInfo mTickingInfo = new Gson().fromJson(data, type);
        if (mTickingInfo != null) {
            if (tickingInfoCallback != null) {
                tickingInfoCallback.onTicking(mTickingInfo);
            }
        }
    }

    public void setTickingInfoCallback(TickingInfoCallback callback) {
        this.tickingInfoCallback = callback;
    }
    // endregion


    // region Notification
    private NotificationInfo notificationInfoCallback;

    public interface NotificationInfo extends BaseCall {
        void onNotify(WearNotificationInfo info);
    }

    public void setNotificationInfoCallback(NotificationInfo callback) {
        this.notificationInfoCallback = callback;
    }

    private void onNotification(String data) {
        if (notificationInfoCallback == null) {
            return;
        }

        Type type = new TypeToken<WearNotificationInfo>() {
        }.getType();
        WearNotificationInfo info = new Gson().fromJson(data, type);
        if (info != null) {
            notificationInfoCallback.onNotify(info);
        }

    }
    // endregion
}
