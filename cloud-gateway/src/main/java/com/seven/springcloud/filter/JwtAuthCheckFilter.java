package com.seven.springcloud.filter;

import com.alibaba.fastjson.JSON;
import com.seven.springcloud.config.AuthJwtProperties;
import com.seven.springcloud.config.WhiteListProperties;
import com.seven.springcloud.constants.TokenConstants;
import com.seven.springcloud.entities.CommonResult;
import com.seven.springcloud.enums.TokenResultEnum;
import com.seven.springcloud.service.PermissionService;
import com.seven.springcloud.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Configuration
public class JwtAuthCheckFilter {

    private static final String AUTH_TOKEN_URL = "/auth/login";
    private static final String USER_REGISTER_URL = "/user/register";
    private static final String REFRESH_TOKEN_URL = "/auth/token/refresh";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "username";
    public static final String FROM_SOURCE = "from-source";

    @Resource
    private WhiteListProperties whiteListProperties;
    @Resource
    private AuthJwtProperties authJwtProperties;
    @Resource
    private PermissionService permissionService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Bean
    @Order(-101)
    public GlobalFilter jwtAuthGlobalFilter() {

        return (exchange, chain) -> {
            log.info("????????????");
            ServerHttpRequest serverHttpRequest = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            ServerHttpRequest.Builder mutate = serverHttpRequest.mutate();
            String requestUrl = serverHttpRequest.getURI().getPath();

            // ???????????????????????? token ???????????????????????????????????? token ?????????????????? token ??????
            if(AUTH_TOKEN_URL.equals(requestUrl) || USER_REGISTER_URL.equals(requestUrl)) {
                log.info("??????url?????????");
                return chain.filter(exchange);
            }

            // ??? HTTP ?????????????????? JWT ??????
            String token = getToken(serverHttpRequest);
            if (StringUtils.isEmpty(token)) {
                return unauthorizedResponse(exchange, serverHttpResponse, TokenResultEnum.TOKEN_MISSION);
            }

            // ???Token?????????????????????Token????????????
            boolean isJwtNotValid = jwtTokenUtil.isTokenExpired(token);
            if(isJwtNotValid){
                return unauthorizedResponse(exchange, serverHttpResponse, TokenResultEnum.TOKEN_INVALID);
            }
            // ?????? token ????????? userId ????????????
            String userId = jwtTokenUtil.getUserIdFromToken(token);
            String username = jwtTokenUtil.getUserNameFromToken(token);
            if (StringUtils.isEmpty(userId)) {
                return unauthorizedResponse(exchange, serverHttpResponse, TokenResultEnum.TOKEN_INVALID);
            }

            // ???????????????????????????
            addHeader(mutate, USER_ID, userId);
            addHeader(mutate, USER_NAME, username);
            // ??????????????????????????????
            removeHeader(mutate, FROM_SOURCE);
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        };
    }
    //??????????????????
    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }
    //??????????????????
    private void removeHeader(ServerHttpRequest.Builder mutate, String name) {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }
    //????????????????????????UTF-8
    static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    @Bean
    @Order(-100)
    public GlobalFilter permissionGlobalFilter() {

        return (exchange, chain) -> {
            log.info("????????????");
            // ??? HTTP ?????????????????? JWT ??????
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            ServerHttpRequest.Builder mutate = request.mutate();
            String path = request.getURI().getPath();

            //??????????????????????????????
            List<String> whiteList = whiteListProperties.getWhites();
            for(String str : whiteList){
                if(path.contains(str)){
                    log.info("??????????????????{}",request.getURI().getPath());
                    return chain.filter(exchange);
                }
            }

            //String headerToken = request.getHeaders().getFirst(TokenConstants.AUTHENTICATION);

            //??????????????????
            String token = getToken(request);
            boolean permission = hasPermission(token,path);
            if (!permission){
                log.info("??????????????????");
                return unauthorizedResponse(exchange, response, TokenResultEnum.PERMISSION_DENIED);
            }
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        };
    }
    private boolean hasPermission(String token, String path){

        Claims claims = Jwts.parser().setSigningKey(authJwtProperties.getSecret()).parseClaimsJws(token).getBody();
        //??????token???????????????
        String roles = (String) claims.get("roles");
        if(roles!=null){
            List<String> pathList = permissionService.pathGet(roles);
            path = path.replaceFirst("/api",StringUtils.EMPTY);
            for (String api:pathList){
                if(api.equals(path)){
                    return true;
                }
            }
        }
        return false;
    }


    //??????token
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(authJwtProperties.getHeader());
        // ??????????????????????????????????????????????????????
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX))
        {
            token = token.replaceFirst(TokenConstants.PREFIX, StringUtils.EMPTY);
        }
        return token;
    }

    //jwt?????????????????????
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, ServerHttpResponse serverHttpResponse, TokenResultEnum tokenResultEnum) {
        log.warn("token????????????,????????????:{}", exchange.getRequest().getPath());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        CommonResult<Object> responseResult = new CommonResult<>(tokenResultEnum.getCode(), tokenResultEnum.getMessage());
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory()
                .wrap(JSON.toJSONStringWithDateFormat(responseResult, JSON.DEFFAULT_DATE_FORMAT)
                        .getBytes(StandardCharsets.UTF_8));
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }
}
