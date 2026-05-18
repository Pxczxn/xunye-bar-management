package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySaveDTO {

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "活动类型不能为空")
    private String type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String coverImage;

    private Integer status;

    private Integer sort;

}
