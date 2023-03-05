package com.seven.springcloud.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("path_permission")
public class PathPermission {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String permissionCode;
    private String hierarchy;
    private String path;
    private String description;
    private boolean status;
}
