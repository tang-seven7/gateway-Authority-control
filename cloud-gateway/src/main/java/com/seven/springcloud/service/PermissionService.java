package com.seven.springcloud.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(value = "cloud-roles-manage")
public interface PermissionService {
    @GetMapping("/path/get")
    List<String> pathGet(@RequestParam("roles") String roles);
}
