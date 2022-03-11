package cn.openwatch.demo.clock_service.model;

import com.google.gson.Gson;
import com.k2archer.demo.lib_base.utils.LocalStorageKVUtils;
import com.k2archer.lib_network.retrofit.ResponseCallback;
import com.k2archer.lib_network.retrofit.RetrofitManager;
import com.k2archer.lib_network.retrofit.response.ApiResponseBody;
import com.k2archer.lib_network.retrofit.response.ResponseStateCode;

import java.util.HashMap;
import java.util.Map;

import cn.openwatch.demo.clock_service.common.KeyValueConstant;
import cn.openwatch.demo.clock_service.contract.UserContract;
import cn.openwatch.demo.clock_service.presenter.UserPresenter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class UserModel implements UserContract.Model {

    private UserApi userApi;

    public UserModel() {
        userApi = RetrofitManager.get().create(UserApi.class);
    }

    private Call<ApiResponseBody<UserInfo>> loginCall;

    @Override
    public void login(LoginInfo loginInfo, final UserPresenter presenter) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");

        String body = new Gson().toJson(loginInfo);

        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        loginCall = userApi.login(headers, requestBody);

        loginCall.enqueue(new ResponseCallback<ApiResponseBody<UserInfo>>() {
            @Override
            public void onSuccess(ApiResponseBody<UserInfo> responseBody) {
                ResponseStateCode state = ResponseStateCode.codeOf(responseBody.code);
                if (state == ResponseStateCode.SERVER_ERROR
                        || state == ResponseStateCode.BAD_REQUEST
                        || state == ResponseStateCode.BAD_TOKEN) {
                    presenter.showToast(ResponseStateCode.getString(state) + "\n" + responseBody.message);
                    return;
                }
                setUserInfo(responseBody.data);
                presenter.onLogin(responseBody);
            }

            @Override
            public void onFail(String message) {
                presenter.showToast(message);
            }
        });

    }

    public UserInfo getUserInfo() {
        return LocalStorageKVUtils.decodeParcelable(KeyValueConstant.USER_INFO, UserInfo.class);
    }

    private void setUserInfo(UserInfo userInfo) {
        LocalStorageKVUtils.encode(KeyValueConstant.USER_INFO, userInfo);
    }

    @Override
    public boolean isLogin() {
        return getUserInfo() != null;
    }


    @Override
    public void onDestroy() {
        if (loginCall != null) {
            loginCall.cancel();
            loginCall = null;
        }
    }


}
