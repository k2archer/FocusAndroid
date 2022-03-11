package com.k2archer.lib_network.retrofit.convert;

import android.util.Log;

import com.google.gson.Gson;
import com.k2archer.lib_network.BuildConfig;
import com.k2archer.lib_network.retrofit.response.ApiResponseBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class ResponseBodyGsonConverter<T> implements Converter<ResponseBody, T> {
    private Gson gson;
    private Type type;

    public ResponseBodyGsonConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String response = value.string();
        try {
            ApiResponseBody body = gson.fromJson(response, ApiResponseBody.class);
            if (body == null) {
                throw new DataResultException(1, response);
            } else if (BuildConfig.DEBUG){
                Log.d("TAG", "response: " + response);
            }
            if (body.data instanceof ArrayList) {
                if (((ArrayList) body.data).size() <= 0) {
                    body.data = null;
                    response = gson.toJson(body);
                }
            }
            return gson.fromJson(response, type);
        } finally {
            value.close();
        }
    }
}