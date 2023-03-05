package com.seven.springcloud.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.seven.springcloud.entities.UserAccount;

import java.util.Map;

public interface AuthService extends IService<UserAccount> {
    boolean sendMessage(String phoneNum, Map<String,Object> code);
}
