package com.seven.springcloud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seven.springcloud.dao.UserDao;
import com.seven.springcloud.entities.UserAccount;
import com.seven.springcloud.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, UserAccount> implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public int userRegister(UserAccount user) {
        Md5Hash md5Hash = new Md5Hash(user.getPassword(), user.getUsername(),2);
        user.setPassword(md5Hash.toString());
        return userDao.insert(user);
    }

    @Override
    public void passwordUpdate(UserAccount user) {
        Md5Hash md5Hash = new Md5Hash(user.getPassword(), user.getUsername(),2);
        user.setPassword(md5Hash.toString());
        userDao.update(user,new QueryWrapper<UserAccount>().eq("username",user.getUsername()));
    }


}
