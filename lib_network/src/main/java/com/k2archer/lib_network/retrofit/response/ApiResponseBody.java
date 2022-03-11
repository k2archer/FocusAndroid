package com.k2archer.lib_network.retrofit.response;

public class ApiResponseBody<T> {
    public int code;
    public String message;
    public T data;

    public ApiResponseBody() {
    }

    public ApiResponseBody(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static int FAIL = -1;
    public static int SUCCESS = 1;
}
