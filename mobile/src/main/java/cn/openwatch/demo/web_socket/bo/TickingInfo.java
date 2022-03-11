package cn.openwatch.demo.web_socket.bo;

public class TickingInfo {

    private long tickingId;
    private long startTime;
    private long ticking;
    private long endTime;
    private int type;
    private int state;
    private String action;
    private String name = "";
    private String effect = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public static final class TICKING_TYPE {
        public static final int WORKING = 1;
        public static final int RESTING = 2;
        public static final int FINISH = 100;
    }

    public enum TickingState {
        TICKING(1),
        FINISH(2),
        CANCEL(3);

        private final int code;

        public int getCode() {
            return code;
        }

        TickingState(int code) {
            this.code = code;
        }

        public static TickingState codeOf(int code) {
            for (TickingState stateCode : values()) {
                if (stateCode.getCode() == code) {
                    return stateCode;
                }
            }
            throw new RuntimeException("没有找到对应的响应状态");
        }

        public static int indexCode(TickingState state) {
            for (TickingState stateCode : values()) {
                if (stateCode.code == state.code) {
                    return state.code;
                }
            }
            throw new RuntimeException("没有找到对应的状态值");
        }
    }

    public int getState() {
        return state;
    }

    public void setState(TickingState state) {
        this.state = state.getCode();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getTicking() {
        return ticking;
    }

    public void setTicking(long ticking) {
        this.ticking = ticking;
    }


    public long getTickingId() {
        return tickingId;
    }

    public void setTickingId(long tickingId) {
        this.tickingId = tickingId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}