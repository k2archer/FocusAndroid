package cn.openwatch.demo.clock_service.contract;

import com.k2archer.lib_network.retrofit.response.ApiResponseBody;

import cn.openwatch.demo.clock_service.model.LoginInfo;
import cn.openwatch.demo.clock_service.model.UserInfo;
import cn.openwatch.demo.clock_service.presenter.UserPresenter;

public interface UserContract {
    interface Model extends BaseContract.Model {
        void login(LoginInfo loginInfo, final UserPresenter presenter);

        boolean isLogin();
    }

    interface View extends BaseContract.View {
        void onLogin(UserInfo userInfo);
    }

    interface Presenter extends BaseContract.Presenter {
        void login(String userName, String password);

        boolean isLogin();

        void onLogin(ApiResponseBody<UserInfo> requestBody);

    }
}
