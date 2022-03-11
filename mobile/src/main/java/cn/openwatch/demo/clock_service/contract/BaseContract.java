package cn.openwatch.demo.clock_service.contract;

public interface BaseContract {
    interface Model {
        void onDestroy();
    }

    interface View {

        void showToast(String message);

    }

    interface Presenter {

        void showToast(String message);


        void onDestroy();
    }

//    interface Model extends BaseContract.Model {
//    }
//
//    interface View extends BaseContract.View {
//    }
//
//    interface Presenter extends BaseContract.Presenter {
//    }
}
