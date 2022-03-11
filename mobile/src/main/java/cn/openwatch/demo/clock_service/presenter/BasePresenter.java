package cn.openwatch.demo.clock_service.presenter;

import androidx.annotation.NonNull;

import com.k2archer.lib_network.retrofit.response.ApiResponseBody;

import cn.openwatch.demo.clock_service.contract.BaseContract;
import cn.openwatch.demo.clock_service.model.UserInfo;
import okhttp3.RequestBody;

public abstract class BasePresenter implements BaseContract.Presenter {

    protected BaseContract.Model model;

    protected BaseContract.View mView;


    protected void BasePresenter(@NonNull BaseContract.View view, @NonNull BaseContract.Model model) {
        mView = view;
        this.model = model;
    }

    private void addPresenter(@NonNull BaseContract.Presenter presenter) {
//        if (mView != null) {
//            mView.addPresenter(presenter);
//        }
    }

    @Override
    public void showToast(String message) {

    }


    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
        if (model != null) {
            model.onDestroy();
        }
    }
}
