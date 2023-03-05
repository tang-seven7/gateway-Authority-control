package com.seven.springcloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seven.springcloud.entities.UserAccount;

public interface UserService extends IService<UserAccount> {
    int userRegister(UserAccount user);
    void passwordUpdate(UserAccount user);
}
