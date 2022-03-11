package com.k2archer.lib_network.retrofit.response;

public enum ResponseStateCode {
    /** 成功响应 12xx */
    SUCCESS(1200), // 接口请求成功, 并返回结果
    RESULT(1202),  // 接口请求成功，无结果返回
    FAILURE(1204), // 接口请求成功, 业务失败
    /** 请求失败 14xx */
    BAD_REQUEST(1400), // 无效接口请求
    BAD_TOKEN(1401),   // 无效 Token
    SERVER_ERROR(1500);// 服务器错误

    public static String getString(ResponseStateCode state) {
        for (ResponseStateCode stateCode : values()) {
            if (state == SUCCESS) {
                return "接口请求成功, 并返回结果";
            } else if (state == RESULT) {
                return "接口请求成功, 无结果返回";
            }else if (state == FAILURE) {
                return "接口请求成功, 业务失败";
            }else if (state == BAD_REQUEST) {
                return "无效接口请求";
            }else if (state == BAD_TOKEN) {
                return "无效 Token";
            }else if (state == SERVER_ERROR) {
                return "服务器错误";
            }
        }
        throw new RuntimeException("没有找到对应的状态值");
    }

    private final int code;

    public int getCode() {
        return code;
    }

    ResponseStateCode(int code) {
        this.code = code;
    }

    public static ResponseStateCode codeOf(int code) {
        for (ResponseStateCode stateCode : values()) {
            if (stateCode.getCode() == code) {
                return stateCode;
            }
        }
        throw new RuntimeException("没有找到对应的响应状态");
    }

    public static int indexCode(ResponseStateCode state) {
        for (ResponseStateCode stateCode : values()) {
            if (stateCode.code == state.code) {
                return state.code;
            }
        }
        throw new RuntimeException("没有找到对应的状态值");
    }
}