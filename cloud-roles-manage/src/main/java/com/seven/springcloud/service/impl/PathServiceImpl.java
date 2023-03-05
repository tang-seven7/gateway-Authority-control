package com.seven.springcloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seven.springcloud.dao.PathDao;
import com.seven.springcloud.entities.PathPermission;
import com.seven.springcloud.service.PathService;
import org.springframework.stereotype.Service;

@Service
public class PathServiceImpl extends ServiceImpl<PathDao, PathPermission> implements PathService {
}
