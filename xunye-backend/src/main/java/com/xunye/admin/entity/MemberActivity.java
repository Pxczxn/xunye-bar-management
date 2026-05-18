package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("member_activity")
public class MemberActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String coverImage;

    private Integer status;

    private Integer sort;

    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
