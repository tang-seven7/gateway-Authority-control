package com.seven.springcloud.util;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seven.springcloud.service.AuthService;
import com.seven.springcloud.entities.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private AuthService authService;

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

        /**配置权限
        *此处User实体需配置属性perms，用户权限
        *获取当前用户对象
         * */
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
//        Subject subject= SecurityUtils.getSubject();
//        UserAccount currentUser =(UserAccount) subject.getPrincipal();
//        authorizationInfo.addStringPermission(currentUser.getRoles());
//        log.info("用户权限为："+currentUser.getRoles());
        return authorizationInfo;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        //用户名/密码认证
        String username = token.getUsername();
        UserAccount user = authService.getOne(new QueryWrapper<UserAccount>().eq("username",username));

        //为空，即用户名不存在
        if(user==null){
            return null;
        }
        //principal：认证的实体信息，可以是username，也可以是数据库表对应的用户的实体对象
        Object principal = user.getUsername();
        return  new SimpleAuthenticationInfo(user, user.getPassword() , ByteSource.Util.bytes(principal),getName());
    }
}
