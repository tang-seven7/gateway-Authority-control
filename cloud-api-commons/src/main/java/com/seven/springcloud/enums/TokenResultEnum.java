package com.seven.springcloud.enums;


public enum TokenResultEnum {

    SUCCESS(200, "成功"),
    FAIL(412, "失败"),

    TOKEN_INVALID(412, "token 已过期或验证不正确！"),
    TOKEN_SIGNATURE_INVALID(402, "无效的签名"),
    TOKEN_MISSION(407, "token 缺失，用户未登录"),
    PERMISSION_DENIED(401, "用户无权限"),
    REFRESH_TOKEN_INVALID(412, "refreshToken 无效");

    private final int code;
    private final String message;

    TokenResultEnum(int code, String message) {
    this.code = code;
    this.message = message;
    }
    public int getCode() {
    return code;
    }
    public String getMessage() {
    return message;
    }
}
