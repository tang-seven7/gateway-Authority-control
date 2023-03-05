package com.seven.springcloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seven.springcloud.entities.Address;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface UserAddressService extends IService<Address> {
    String searchAddress(@RequestParam("name")String name);
    Boolean searchAllUpdate() throws ExecutionException, InterruptedException;
    Map<String,List<Address>> searchAllNew();
    int bloomAdd(Address address);
    int bloomAdd();
    Map<String,Address> bloomFilter(int id);
}
