package com.seven.springcloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seven.springcloud.entities.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthDao extends BaseMapper<UserAccount> {
}
