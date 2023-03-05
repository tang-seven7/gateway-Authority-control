package com.seven.springcloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seven.springcloud.entities.RolesPermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionDao extends BaseMapper<RolesPermission> {
}
