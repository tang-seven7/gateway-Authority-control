package com.seven.springcloud.enums;

public enum NormalResultEnum {
    SUCCESS(200,"成功"),
    FAIL(412,"失败"),
    SYSTEM_FAIL(444,"系统错误"),
    VALIDATE_FAIL(403,"参数错误"),
    NULL_FAIL(424,"空值"),
    UNKNOWN_ERROR(500, "未知错误");

    private final Integer code;
    private final String message;


    NormalResultEnum(Integer code, String message) {
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
