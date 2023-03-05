package com.seven.springcloud.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("address")
public class Address implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String county;
    private String village;
    private String longKey;
}
