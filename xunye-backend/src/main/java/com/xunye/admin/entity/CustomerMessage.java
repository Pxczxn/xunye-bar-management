package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_message")
public class CustomerMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String title;

    private String content;

    private String type;

    private Integer isRead;

    private Long relatedOrderId;

    private LocalDateTime createdAt;

}
