package com.seven.springcloud.controller;

import com.seven.springcloud.entities.CommonResult;
import com.seven.springcloud.entities.UserAccount;
import com.seven.springcloud.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserManageController {
    @Resource
    private UserService userService;

    //普通用户注册
    @PostMapping("/register")
    public CommonResult<Object> userRegister(@RequestBody UserAccount user){
        user.setRoles("user");
        return CommonResult.success(userService.userRegister(user));
    }
}
