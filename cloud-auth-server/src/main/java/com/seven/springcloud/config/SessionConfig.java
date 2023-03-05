package com.seven.springcloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        //cookieSerializer.setDomainName("");     //指定cookie作用域
        cookieSerializer.setCookieName("authSession");     //指定cookie名字
        return cookieSerializer;
    }

//    //序列化
//    @Bean
//    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
//        return new GenericJackson2JsonRedisSerializer();
//    }

}
