package com.xunye.admin.enums;

import lombok.Getter;

@Getter
public enum OrderSource {

    ADMIN_POS("ADMIN_POS", "吧台点单"),
    CUSTOMER_MINI("CUSTOMER_MINI", "顾客扫码");

    private final String code;
    private final String description;

    OrderSource(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderSource fromCode(String code) {
        for (OrderSource source : values()) {
            if (source.code.equals(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("未知的订单来源: " + code);
    }
}
