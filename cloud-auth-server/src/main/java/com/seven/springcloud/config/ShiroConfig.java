package com.seven.springcloud.config;

import com.seven.springcloud.util.UserRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(@Qualifier("getDefaultWebSecurityManager") DefaultWebSecurityManager defaultWebSecurityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);
        //拦截页面
        Map<String, String> filterMap = new LinkedHashMap<>();
        //登录/登出/管理员注册，所有人的权限
        filterMap.put("/user/login", "anon");
        filterMap.put("/user/logout", "anon");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);
        //未授权页面
        shiroFilterFactoryBean.setLoginUrl("/user/show");
        shiroFilterFactoryBean.setUnauthorizedUrl("/user/unauthorized");

        return shiroFilterFactoryBean;
    }

    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager(@Qualifier("userRealm") UserRealm userRealm) {
        DefaultWebSecurityManager SecurityManager = new DefaultWebSecurityManager();
        SecurityManager.setRealm(userRealm);
        return SecurityManager;
    }

    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        //注册MD5加密
        userRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return userRealm;
    }


    /**
     * 设置shiro加密方式
     *
     * @return HashedCredentialsMatcher
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher(){
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        // 使用md5 算法进行加密
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        // 设置散列次数： 意为加密几次
        hashedCredentialsMatcher.setHashIterations(2);

        return hashedCredentialsMatcher;
    }

}
