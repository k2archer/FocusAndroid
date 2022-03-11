package cn.openwatch.demo.bean;

public class ResponseBody<T> {
    private int code;
    private String msg;
    private T data;

    public ResponseBody() {
    }

    public ResponseBody(int code, String message, T data) {
        this.code = code;
        this.msg = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
