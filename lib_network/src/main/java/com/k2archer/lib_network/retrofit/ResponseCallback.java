package com.k2archer.lib_network.retrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ResponseCallback<T> implements BaseStorageCallback<T>, Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {

        if (response.code() == 200 && response.body() != null) {
            onSuccess(response.body());
        } else {
            Exception e = new Exception(response.code() + " " + response.message());
            onFailure(call, e);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String message = ExceptionUtils.handleRequestException(call, t);
        onFail(message);

    }
}
