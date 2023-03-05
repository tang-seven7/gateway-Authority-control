package com.seven.springcloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seven.springcloud.entities.Address;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAddressMapper extends BaseMapper<Address> {
}
