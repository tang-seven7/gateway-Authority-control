package com.seven.springcloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seven.springcloud.entities.CommonResult;
import com.seven.springcloud.entities.PathPermission;
import com.seven.springcloud.entities.RolesPermission;
import com.seven.springcloud.service.PathService;
import com.seven.springcloud.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.util.*;

@RequestMapping("/path")
@RestController
@Slf4j
public class PathPermissionController {
    @Resource
    private PathService pathService;
    @Resource
    private PermissionService permissionService;

    @GetMapping("/add")
    public boolean pathAdd(@RequestBody PathPermission pathPermission){
        log.info(pathPermission.toString());
        return pathService.save(pathPermission);
    }

    @PostMapping("/permission/add")
    public boolean permissionAdd(@RequestBody Map<String,Object> map) {
        String roles = (String) map.get("roles");
        List<String> codeList = (List<String>) map.get("codeList");
        return permissionService.permissionAdd(roles, codeList);
    }

    @GetMapping("/get")
    public List<String> pathGet(@RequestParam("roles") String roles) {
        RolesPermission permission = permissionService.getOne(new QueryWrapper<RolesPermission>().eq("role",roles));
        String codes =  StringUtils.strip(permission.getPermissionCode(), "[]");
        List<String> list = Arrays.asList(codes.split(","));
        List<String> pathList = new ArrayList<>();
        for(String code:list){
            String api = pathService.getOne(new QueryWrapper<PathPermission>().eq("permission_code",code.trim())).getPath();
            pathList.add(api);
        }
        return pathList;
    }
}
