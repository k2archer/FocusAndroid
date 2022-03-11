package com.k2archer.lib_network.retrofit;

public interface BaseStorageCallback<T> {

    public abstract void onSuccess(T responseBody);

    public abstract void onFail(String message);
}
