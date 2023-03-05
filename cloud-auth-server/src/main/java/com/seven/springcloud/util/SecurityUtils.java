package com.seven.springcloud.util;


import com.seven.springcloud.constants.TokenConstants;
import javax.servlet.http.HttpServletRequest;
import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * 权限获取工具类
 *
 */
public class SecurityUtils
{

    /**
     * 根据request获取请求token
     */
    public static String getToken(HttpServletRequest request)
    {
        // 从header获取token标识
        String token = request.getHeader(TokenConstants.AUTHENTICATION);
        return replaceTokenPrefix(token);
    }

    /**
     * 裁剪token前缀
     */
    public static String replaceTokenPrefix(String token) {
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (!isEmpty(token) && token.startsWith(TokenConstants.PREFIX)) {
            token = token.replaceFirst(TokenConstants.PREFIX, "");
        }
        return token;
    }

}
