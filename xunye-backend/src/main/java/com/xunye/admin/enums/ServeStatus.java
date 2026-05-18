package com.xunye.admin.enums;

import lombok.Getter;

@Getter
public enum ServeStatus {

    PENDING("PENDING", "待处理"),
    MAKING("MAKING", "制作中"),
    FINISHED("FINISHED", "已完成");

    private final String code;
    private final String description;

    ServeStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ServeStatus fromCode(String code) {
        for (ServeStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的履约状态: " + code);
    }
}
