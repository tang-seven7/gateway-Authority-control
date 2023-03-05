package com.seven.springcloud.controller;

import com.seven.springcloud.service.UserAddressService;
import com.seven.springcloud.entities.Address;
import com.seven.springcloud.entities.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/address")
@Slf4j
public class UserAddressController {
    @Resource
    private UserAddressService userAddressService;

    @GetMapping("/get/all")
    public CommonResult<Object> searchPlaceAll() {
        return CommonResult.success(userAddressService.list());
    }

    //添加新地址
    @PostMapping("/add")
    public CommonResult<Object> add(@RequestBody Address address){
        int i = userAddressService.bloomAdd(address);
        return CommonResult.success(i);
    }
    //将所有数据库中数据id存入布隆过滤器
    @PostMapping("/bloom/add")
    public CommonResult<Object> bloomAdd(){
        return CommonResult.success(userAddressService.bloomAdd());
    }

    @GetMapping("/get")
    public CommonResult<Object> get(@RequestParam("id") int id, HttpServletRequest request){
        Map<String,Address> map = userAddressService.bloomFilter(id);
        if(map!=null){
            return CommonResult.success(map);
        }else {
            return CommonResult.fail(null);
        }

    }

}
