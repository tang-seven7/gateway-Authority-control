package com.seven.springcloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seven.springcloud.entities.PathPermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PathDao extends BaseMapper<PathPermission> {
}
