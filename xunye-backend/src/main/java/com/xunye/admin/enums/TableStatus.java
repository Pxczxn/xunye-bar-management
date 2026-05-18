package com.xunye.admin.enums;

import lombok.Getter;

@Getter
public enum TableStatus {

    EMPTY("EMPTY", "空闲"),
    USING("USING", "使用中"),
    CLEANING("CLEANING", "清洁中"),
    DISABLED("DISABLED", "已停用");

    private final String code;
    private final String description;

    TableStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TableStatus fromCode(String code) {
        for (TableStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的桌台状态: " + code);
    }
}
