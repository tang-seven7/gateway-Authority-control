package com.seven.springcloud.util;

import com.seven.springcloud.config.AuthJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class JwtTokenUtil {

    private static final String JWT_CACHE_KEY = "jwt:userId:";
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "username";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String EXPIRE_IN = "expire_in";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AuthJwtProperties jwtProperties;

    /**
     * 生成 token 令牌主方法
     * @param userId 用户Id或用户名
     * @return 令token牌
     */

    public Map<String, Object> generateTokenAndRefreshToken(String userId, String username, String roles) {
        //生成令牌及刷新令牌
        Map<String, Object> tokenMap = buildToken(userId, username, roles);
        //redis缓存结果
        cacheToken(userId, tokenMap);
        return tokenMap;
    }

    //将token缓存进redis
    private void cacheToken(String userId, Map<String, Object> tokenMap) {
        stringRedisTemplate.opsForHash().put(JWT_CACHE_KEY + userId, ACCESS_TOKEN, tokenMap.get(ACCESS_TOKEN));
        stringRedisTemplate.opsForHash().put(JWT_CACHE_KEY + userId, REFRESH_TOKEN, tokenMap.get(REFRESH_TOKEN));
        stringRedisTemplate.expire(userId, jwtProperties.getExpiration() * 2, TimeUnit.MILLISECONDS);
    }
    //生成令牌
    private Map<String, Object> buildToken(String userId, String username, String roles) {
        Map<String, String> map = new HashMap<>();
        map.put("roles",roles);
        //生成token令牌
        String accessToken = generateToken(userId, username, map);
        //生成刷新令牌
        String refreshToken = generateRefreshToken(userId, username, null);
        //存储两个令牌及过期时间，返回结果
        HashMap<String, Object> tokenMap = new HashMap<>(2);
        tokenMap.put(ACCESS_TOKEN, accessToken);
        tokenMap.put(REFRESH_TOKEN, refreshToken);
        tokenMap.put(EXPIRE_IN, jwtProperties.getExpiration());
        return tokenMap;
    }
    /**
     * 生成 token 令牌 及 refresh token 令牌
     * @param payloads 令牌中携带的附加信息
     * @return 令牌
     */
    public String generateToken(String userId, String username,
                                Map<String,String> payloads) {
        Map<String, Object> claims = buildClaims(userId, username, payloads);;

        return generateToken(claims);
    }
    public String generateRefreshToken(String userId, String username, Map<String,String> payloads) {
        Map<String, Object> claims = buildClaims(userId, username, payloads);

        return generateRefreshToken(claims);
    }
    //构建map存储令牌需携带的信息
    private Map<String, Object> buildClaims(String userId, String username, Map<String, String> payloads) {
        int payloadSizes = payloads == null? 0 : payloads.size();

        Map<String, Object> claims = new HashMap<>(payloadSizes + 2);
        if(payloadSizes!=0){
            claims.put("roles", payloads.get("roles"));
        }
        claims.put("sub", userId);
        claims.put("username", username);
        claims.put("created", new Date());


        if(payloadSizes > 0){
            claims.putAll(payloads);
        }

        return claims;
    }


    /**
     * 刷新令牌并生成新令牌
     * 并将新结果缓存进redis
     */
    public Map<String, Object> refreshTokenAndGenerateToken(String userId, String username,String roles) {
        Map<String, Object> tokenMap = buildToken(userId, username,roles);
        stringRedisTemplate.delete(JWT_CACHE_KEY + userId);
        cacheToken(userId, tokenMap);
        return tokenMap;
    }

    /**
     * 从request获取userid
     * @param request http请求
     * @return request.getHeader
     */
    public String getUserIdFromRequest(HttpServletRequest request) {
        return request.getHeader(USER_ID);
    }

    //缓存中删除token
    public boolean removeToken(String userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(JWT_CACHE_KEY + userId));
    }


    /**
     * 从令牌中获取用户id
     *
     * @param token 令牌
     * @return 用户id
     */
    public String getUserIdFromToken(String token) {
        String userId;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }
    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get(USER_NAME);
        } catch (Exception e) {
            username = null;
        }
        return username;
    }


    /**
     * 判断令牌是否不存在 redis 中
     *
     * @param token 刷新令牌
     * @return true=不存在，false=存在
     */
    public Boolean isRefreshTokenNotExistCache(String token) {
        String userId = getUserIdFromToken(token);
        String refreshToken = (String)stringRedisTemplate.opsForHash().get(JWT_CACHE_KEY + userId, REFRESH_TOKEN);
        return refreshToken == null || !refreshToken.equals(token);
    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return true=已过期，false=未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            //验证 JWT 签名失败等同于令牌过期
            return true;
        }
    }

    /**
     * 刷新令牌
     *
     * @param token 原令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            Claims claims = getClaimsFromToken(token);
            claims.put("created", new Date());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    /**
     * 验证令牌
     *
     * @param token       令牌
     * @param userId  用户Id用户名
     * @return 是否有效
     */
    public Boolean validateToken(String token, String userId) {

        String username = getUserIdFromToken(token);
        return (username.equals(userId) && !isTokenExpired(token));
    }


    /**
     * 生成令牌
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis()
                + jwtProperties.getExpiration());
        return Jwts.builder().setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512,
                        jwtProperties.getSecret())
                .compact();
    }
    /**
     * 生成刷新令牌 refreshToken，有效期是令牌的 2 倍
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateRefreshToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 2);
        return Jwts.builder().setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    /**
     * 从令牌中获取数据声明,验证 JWT 签名
     * @param token 令牌
     * @return 数据声明
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }
}