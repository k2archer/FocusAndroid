package cn.openwatch.demo.clock_service.model;

import androidx.media.VolumeProviderCompat;

import com.k2archer.lib_network.retrofit.response.ApiResponseBody;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserApi {

    @POST("api/user/login")
    @Headers("Content-Type: application/json")
    Call<ApiResponseBody<UserInfo>> login(@HeaderMap Map<String, String> headers, @Body RequestBody body);
//    @Field("username") String username, @Field("password") String userPassword

}
