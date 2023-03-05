package com.seven.springcloud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seven.springcloud.dao.PermissionDao;
import com.seven.springcloud.entities.RolesPermission;
import com.seven.springcloud.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class PermissionServiceImpl extends ServiceImpl<PermissionDao, RolesPermission> implements PermissionService {
    @Resource
    private PermissionDao permissionDao;
    @Override
    public boolean permissionAdd(String roles, List<String> codeList) {
        RolesPermission rolesPermission = new RolesPermission();
        rolesPermission.setRole(roles);
        int result = 0;
        RolesPermission role = permissionDao.selectOne(new QueryWrapper<RolesPermission>().eq("role",roles));

        if(role!=null){
            //去掉头尾括号，并转为列表
            List<String> list = new java.util.ArrayList<>(Collections.singletonList(
                    StringUtils.strip(role.getPermissionCode(), "[]")));
            //将新数据添加至列表
            list.addAll(codeList);

            rolesPermission.setPermissionCode(list.toString());
            rolesPermission.setId(role.getId());
            result = permissionDao.updateById(rolesPermission);
        }else {
            rolesPermission.setPermissionCode(codeList.toString());
            result = permissionDao.insert(rolesPermission);
        }
        return result > 0;
    }
}
