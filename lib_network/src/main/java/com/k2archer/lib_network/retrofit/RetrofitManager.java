package com.k2archer.lib_network.retrofit;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.k2archer.lib_network.BuildConfig;
import com.k2archer.lib_network.retrofit.convert.ApiGsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class RetrofitManager {

    private RetrofitManager() {

    }

    public static Retrofit get() {
        return RetrofitInstance.sRetrofit;
    }

    public static final String BASE_URL = "http://192.168.0.153:8080/";

    private static class RetrofitInstance {
        private static Retrofit sRetrofit = buildRetrofit(BASE_URL);
    }


    private static Retrofit buildRetrofit(String baseUrl) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor);
            okHttpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request()
                            .newBuilder()
                            .removeHeader("User-Agent") // 移除旧的
                            .addHeader("token", "token1").build();
                    return chain.proceed(request);
                }
            });
            okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
        } else {
            okHttpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    // 设置原生的 Http 请求 User-Agent  参数
                    Request request = chain.request()
                            .newBuilder()
                            .removeHeader("User-Agent") // 移除旧的
//                            .addHeader("User-Agent", WebSettings.getDefaultUserAgent(MiGeApplication.getContext())) // 添加真正的头部
                            .build();
                    return chain.proceed(request);
                }
            });
        }

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(ApiGsonConverterFactory.create())
                .build();
    }

}
