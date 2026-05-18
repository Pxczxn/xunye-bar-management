package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerMessageVO {

    private Long id;

    private String title;

    private String content;

    private String type;

    private Integer isRead;

    private Long relatedOrderId;

    private LocalDateTime createdAt;

}
