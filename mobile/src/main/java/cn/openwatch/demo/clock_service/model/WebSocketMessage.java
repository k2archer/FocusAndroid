package cn.openwatch.demo.clock_service.model;

public class WebSocketMessage<T> {
    private String action;
    private T data;

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
