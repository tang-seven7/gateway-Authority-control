package com.seven.springcloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seven.springcloud.enums.LoginResultEnum;
import com.seven.springcloud.enums.TokenResultEnum;
import com.seven.springcloud.service.AuthService;
import com.seven.springcloud.entities.CommonResult;
import com.seven.springcloud.entities.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.seven.springcloud.util.JwtTokenUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequestMapping("/auth")
@RestController
@Slf4j
public class AuthController {

    @Resource
    private AuthService authService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    @PostMapping("/login")
    public CommonResult<Object> login(@RequestBody @Validated UserAccount user){

        String username=user.getUsername();
        String password=user.getPassword();

        //shiro验证
        Subject subject= SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);

        try {
            subject.login(token);
        } catch (UnknownAccountException e) {
            log.info("登录用户不存在");
            return new CommonResult<>(LoginResultEnum.USER_NULL.getCode(), LoginResultEnum.USER_NULL.getMessage());
        } catch (IncorrectCredentialsException e) {
            log.info("登录密码错误");
            return new CommonResult<>(LoginResultEnum.PASSWORD_WRONG.getCode(), LoginResultEnum.PASSWORD_WRONG.getMessage());
        }catch (AuthenticationException e) {
            log.warn("用户登录异常:" + e.getMessage());
            return CommonResult.system_fail(null);
        }

        //Cookie sso_token = new Cookie("sso_token", UUID.randomUUID().toString());
        //response.addCookie(sso_token);

        //获取登录用户信息
        UserAccount account = authService.getOne(new QueryWrapper<UserAccount>().eq("username",username));
        // 通过 jwtTokenUtil 生成 JWT 令牌和刷新令牌
        Map<String, Object> tokenMap = jwtTokenUtil
                .generateTokenAndRefreshToken(String.valueOf(account.getId()), username,
                        //用户角色映射表中中查询用户角色
                        authService.getOne(new QueryWrapper<UserAccount>().eq("username",username)).getRoles());
        return CommonResult.success(tokenMap);
    }

    //利用阿里云发送短信验证码
    @GetMapping("/send/{phone}")
    public CommonResult<Object> codeSend(@PathVariable("phone") String phone){
        String code = (String) redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)){
            log.info("验证码存在，未过期");
        }
        //不存在，生成验证码
        code = UUID.randomUUID().toString().substring(0,4);
        HashMap<String,Object> map = new HashMap<>();
        map.put("code",code);

        boolean isSend = authService.sendMessage(phone,map);
        if(isSend){
            //存储验证码
            redisTemplate.opsForValue().set(phone,code, 30,TimeUnit.SECONDS);
            return CommonResult.success(null);
        }else {
            return CommonResult.fail(null);
        }
        //redisTemplate.opsForValue().set(phone,code, 300,TimeUnit.SECONDS);
        //return CommonResult.success(map);
    }

    /**
     * 刷新JWT令牌,用旧的令牌换新的令牌
     * 参数为需要刷新的令牌
     * header中携带刷新令牌
     */
    @GetMapping("/token/refresh")
    public CommonResult<Object> refreshToken(@RequestHeader(value = "${auth.jwt.header}") String token){
        token = com.seven.springcloud.util.SecurityUtils.replaceTokenPrefix(token);

        if (StringUtils.isEmpty(token)) {

            return new CommonResult<>(TokenResultEnum.TOKEN_MISSION.getCode(),
                    TokenResultEnum.TOKEN_MISSION.getMessage());
        }

        // 对Token解签名，并验证Token是否过期
        boolean isJwtNotValid = jwtTokenUtil.isTokenExpired(token);
        if(isJwtNotValid){
            return new CommonResult<>(TokenResultEnum.TOKEN_INVALID.getCode(),
                    TokenResultEnum.TOKEN_INVALID.getMessage());
        }

        // 验证 token 里面的 userId 是否为空
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUserNameFromToken(token);
        if (StringUtils.isEmpty(userId)) {
            return new CommonResult<>(TokenResultEnum.TOKEN_INVALID.getCode(),
                    TokenResultEnum.TOKEN_INVALID.getMessage());
        }

        // 这里为了保证 refreshToken 只能用一次，刷新后，会从 redis 中删除。
        // 如果用的不是 redis 中的 refreshToken 进行刷新令牌，则不能刷新。
        // 如果使用 redis 中已过期的 refreshToken 也不能刷新令牌。
        boolean isRefreshTokenNotExisted = jwtTokenUtil.isRefreshTokenNotExistCache(token);
        if(isRefreshTokenNotExisted){
            return new CommonResult<>(TokenResultEnum.TOKEN_INVALID.getCode(),
                    TokenResultEnum.TOKEN_INVALID.getMessage());
        }

        //String us = jwtTokenUtil.getUserIdFromToken(token);
        Map<String, Object> tokenMap = jwtTokenUtil.refreshTokenAndGenerateToken(userId, username,"admin");

        return CommonResult.success(tokenMap);
    }


    @PostMapping("/logout")
    public CommonResult<Object> logout(@RequestParam("username") String username, HttpServletRequest request) {
        //清楚token
        boolean logoutResult = jwtTokenUtil.removeToken(username);
        if (logoutResult) {
            //清除所有session数据
            Enumeration<String> em = request.getSession().getAttributeNames();
            while(em.hasMoreElements()){
                request.getSession().removeAttribute(em.nextElement());
            }
            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated()) {
                //清除UserToken数据
                subject.logout();
            }
            return CommonResult.success(null);
        } else {
            return CommonResult.fail(null);
        }
    }

}
