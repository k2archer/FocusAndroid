package com.k2archer.lib_network.retrofit;


import android.net.ParseException;
import android.util.Log;

import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import com.k2archer.lib_network.BuildConfig;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.Call;


public class ExceptionUtils {

    /**
     * HTTP 状态码
     **/
    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 402;
    private static final int NOT_FOUND = 404;
    private static final int METHOD_NOT_ALLOWED = 405;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int CONFLICT = 409;
    private static final int PRECONDITION_FAILED = 412;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;

    public static String handleException(Throwable e) {
        String exceptionMessage;
        if (e instanceof retrofit2.HttpException) {
            retrofit2.HttpException httpException = (retrofit2.HttpException) e;
            switch (httpException.code()) {
                case BAD_REQUEST:
                case UNAUTHORIZED:
                case FORBIDDEN:
                case NOT_FOUND:
                case METHOD_NOT_ALLOWED:
                case REQUEST_TIMEOUT:
                case CONFLICT:
                case PRECONDITION_FAILED:
                case GATEWAY_TIMEOUT:
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                    // 均视为网络错误
                default:
                    exceptionMessage = ("网络错误 " + httpException.code());
                    break;
            }
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException
                || e instanceof MalformedJsonException) {
            // 均视为解析错误
            exceptionMessage = "解析错误";
        } else if (e instanceof ConnectException) {
            // 均视为网络错误
            exceptionMessage = "连接失败，网络异常或无法访问服务器！";
        } else if (e instanceof java.net.UnknownHostException) {
            // 网络未连接
            exceptionMessage = "网络未连接";
        } else if (e instanceof SocketTimeoutException) {
            // 服务器连接超时
            exceptionMessage = "服务器响应超时";
        } else {
            // 未知错误
            exceptionMessage = "未知错误 " + e.getMessage();
        }
        return exceptionMessage;
    }

    public static String handleRequestException(Call<?> call, Throwable e) {
        // todo redo
        String message = handleException(e);
        String log = call.request().url().toString() + " 请求失败 " + message + " " + e.toString();
        System.out.println(log);
        return message;
    }

}