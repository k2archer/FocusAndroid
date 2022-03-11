package cn.openwatch.demo.clock_service.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class UserInfo implements Parcelable {

    private String token;

    public UserInfo() {
        token = "";
    }

    protected UserInfo(Parcel in) {
        token = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
    }
}
