package com.seven.springcloud.entities;

import com.seven.springcloud.enums.NormalResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T>{
    private Integer code;
    private String message;
    private T      data;
    private int dataSize;

    public CommonResult(Integer code, String message){
        this(code, message, null, 0);
    }

    public CommonResult(Integer code, String message, T data){
        this(code, message, data, 0);
    }

    public static <T> CommonResult<T> success(T data){
        return new CommonResult<T>(NormalResultEnum.SUCCESS.getCode(), NormalResultEnum.SUCCESS.getMessage(),data,0);
    }

    public static <T> CommonResult<T> fail(T data){
        return new CommonResult<T>(NormalResultEnum.FAIL.getCode(), NormalResultEnum.FAIL.getMessage(),data,0);
    }

    public static <T> CommonResult<T> validate_fail(T data){
        return new CommonResult<T>(NormalResultEnum.VALIDATE_FAIL.getCode(), NormalResultEnum.VALIDATE_FAIL.getMessage(),data,0);
    }

    public static <T> CommonResult<T> system_fail(T data){
        return new CommonResult<T>(NormalResultEnum.SYSTEM_FAIL.getCode(), NormalResultEnum.SYSTEM_FAIL.getMessage(),data,0);
    }
    public static <T> CommonResult<T> null_fail(T data){
        return new CommonResult<T>(NormalResultEnum.NULL_FAIL.getCode(), NormalResultEnum.NULL_FAIL.getMessage(),data,0);
    }

}
