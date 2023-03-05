package com.seven.springcloud.enums;

public enum LoginResultEnum {

    USER_NULL(424,"用户不存在"),
    PERMISSION_DENIED(401, "用户无权限"),
    PASSWORD_WRONG(400, "密码错误");

    private final Integer code;
    private final String message;


    LoginResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
