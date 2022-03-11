package cn.openwatch.demo.clock_service.presenter;

import com.k2archer.lib_network.retrofit.response.ApiResponseBody;
import com.k2archer.lib_network.retrofit.response.ResponseStateCode;

import cn.openwatch.demo.clock_service.contract.BaseContract;
import cn.openwatch.demo.clock_service.contract.UserContract;
import cn.openwatch.demo.clock_service.model.LoginInfo;
import cn.openwatch.demo.clock_service.model.UserInfo;
import cn.openwatch.demo.clock_service.model.UserModel;

public class UserPresenter extends BasePresenter implements UserContract.Presenter {

    public UserPresenter(BaseContract.View view) {
        BasePresenter(view, new UserModel());
    }


    @Override
    public void login(String userName, String password) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUsername(userName);
        loginInfo.setPassword(password);

        ((UserModel) model).login(loginInfo, this);
    }

    @Override
    public boolean isLogin() {
        return ((UserModel) model).isLogin();
    }

    @Override
    public void onLogin(ApiResponseBody<UserInfo> responseBody) {
        ResponseStateCode state = ResponseStateCode.codeOf(responseBody.code);
        if (state == ResponseStateCode.SUCCESS) {
            ((UserContract.View) mView).onLogin(responseBody.data);
        } else if (state == ResponseStateCode.FAILURE) {
            ((UserContract.View) mView).showToast(responseBody.message);
        }
    }


    public UserInfo getUserInfo() {
        return ((UserModel) model).getUserInfo();
    }
}
