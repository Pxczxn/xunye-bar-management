package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("member_activity")
public class MemberActivity extends BaseEntity {

    private String title;

    private String description;

    private String type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String coverImage;

    private String settings;

    private Integer status;

    private Integer sort;

}
