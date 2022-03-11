package com.k2archer.demo.common.path;

public class WearTickingInfo<T> {
    private String action;

    public static class TickingAction {
        public static final String START_TICKING = "startTicking";
        public static final String CANCEL_TICKING = "cancelTicking";
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private long tickingId;
    private long startTime;
    private long ticking;
    private long endTime;
    private int type;
    private String name = "";


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final class TICKING_TYPE {
        public static final int WORKING = 1;
        public static final int RESTING = 2;
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



}
