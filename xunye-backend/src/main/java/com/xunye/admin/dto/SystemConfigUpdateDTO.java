package com.xunye.admin.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SystemConfigUpdateDTO {

    /**
     * 配置键值对
     */
    private Map<String, String> configs;

}
