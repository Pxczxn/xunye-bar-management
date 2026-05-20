package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_points_record")
public class CustomerPointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String title;

    private Integer amount;

    private String relatedOrderNo;

    private LocalDateTime createdAt;

}
