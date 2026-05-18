package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityVO {

    private Long id;

    private String title;

    private String description;

    private String type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String coverImage;

    private Integer status;

    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
