package com.k2archer.demo.lib_base.utils.storage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class BasePreference {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public BasePreference(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
}
