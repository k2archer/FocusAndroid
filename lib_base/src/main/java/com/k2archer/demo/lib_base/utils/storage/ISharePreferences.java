package com.k2archer.demo.lib_base.utils.storage;

import java.util.Set;


public interface ISharePreferences {

    boolean putString(String key, String value);

    boolean putStringSet(String key, Set<String> values);

    boolean putInt(String key, int value);

    boolean putLong(String key, long value);

    boolean putFloat(String key, float value);

    boolean putBoolean(String key, boolean value);

    boolean remove(String key);

    boolean clear();

    boolean commit();

    void apply();

    String getString(String key, String defValue);

    int getInt(String key, int defValue);

    long getLong(String key, long defValue);

    float getFloat(String key, float defValue);

    boolean getBoolean(String key, boolean defValue);
}
