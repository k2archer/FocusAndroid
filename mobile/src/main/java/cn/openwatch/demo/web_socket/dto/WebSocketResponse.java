package cn.openwatch.demo.web_socket.dto;

public class WebSocketResponse<T> {

    private int code;
    private String msg;
    private String action;
    private T data;

    public class Action {
        public static final String TICKING = "ticking";
    }

    public WebSocketResponse() {
    }

    public WebSocketResponse(int code, String message, String action,  T data) {
        this.code = code;
        this.msg = message;
        this.action = action;
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


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
