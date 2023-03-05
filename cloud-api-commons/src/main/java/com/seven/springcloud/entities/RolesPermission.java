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
@TableName("roles_permission")
public class RolesPermission {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String role;
    private String permissionCode;
}
