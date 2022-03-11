package cn.openwatch.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Set;

public class LocalStorageKVUtils {

    private static class SingletonClassInstance {
        private static Context context = null;

        SingletonClassInstance(Context context) {
            SingletonClassInstance.context = context;
        }
    }

    public static void init(Context context) {
        new SingletonClassInstance(context);
    }

    private static Context getContext() {
        return SingletonClassInstance.context;
    }

    private static SharedPreferences getSharedPreferences() {
        if (getContext() != null) {
            return getContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
        }
        return null;
    }

    public static final String CONFIG_NAME = "focus";

    public static String decodeString(String key, String defValue) {
        return getSharedPreferences().getString(key, defValue);
    }

    public static int decodeInt(String key, int defValue) {
        return getSharedPreferences().getInt(key, defValue);
    }

    public static  long decodeLong(String key, long defValue) {
        return getSharedPreferences().getLong(key, defValue);
    }

    public static float decodeFloat(String key, float defValue) {
        return getSharedPreferences().getFloat(key, defValue);
    }

    public static  boolean decodeBoolean(String key, boolean defValue) {
        return getSharedPreferences().getBoolean(key, defValue);
    }


    public static <T extends Parcelable> T decodeParcelable(String key, Class<T> tClass) {
        String value_json = getSharedPreferences().getString(key, "");
        return new Gson().fromJson(value_json, tClass);
    }


    public static  boolean encodeString(String key, String value) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.putString(key, value);
        e.apply();
        return true;
    }

    public static boolean encodeStringSet(String key, Set<String> values) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.putStringSet(key, values);
        e.apply();
        return true;
    }

    public static boolean encodeInt(String key, int value) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.putInt(key, value);
        e.apply();
        return true;
    }

    boolean encodeLong(String key, long value) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.putLong(key, value);
        e.apply();
        return true;
    }

    public static  boolean encodeFloat(String key, float value) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.putFloat(key, value);
        e.apply();
        return true;
    }

    public static boolean encodeBoolean(String key, boolean value) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.putBoolean(key, value);
        e.apply();
        return true;
    }

    public static boolean remove(String key) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.remove(key);
        e.apply();
        return true;
    }

    public static boolean encode(String key, @Nullable Parcelable value) {
        SharedPreferences.Editor e = getSharedPreferences().edit();
        e.putString(key, new Gson().toJson(value));
        e.apply();
        return true;
    }

}
