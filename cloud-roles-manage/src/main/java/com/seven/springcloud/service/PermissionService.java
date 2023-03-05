package com.seven.springcloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seven.springcloud.entities.PathPermission;
import com.seven.springcloud.entities.RolesPermission;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface PermissionService extends IService<RolesPermission> {
    boolean permissionAdd(String roles, List<String> codeList);
}
