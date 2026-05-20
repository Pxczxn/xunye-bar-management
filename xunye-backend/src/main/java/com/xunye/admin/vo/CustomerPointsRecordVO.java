package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerPointsRecordVO {

    private Long id;

    private String title;

    private Integer amount;

    private String relatedOrderNo;

    private LocalDateTime createdAt;

}
