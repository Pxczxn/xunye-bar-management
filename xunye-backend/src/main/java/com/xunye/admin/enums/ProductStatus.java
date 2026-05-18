package com.xunye.admin.enums;

import lombok.Getter;

@Getter
public enum ProductStatus {

    ON_SALE("ON_SALE", "上架"),
    OFF_SALE("OFF_SALE", "下架");

    private final String code;
    private final String description;

    ProductStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProductStatus fromCode(String code) {
        for (ProductStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的商品状态: " + code);
    }
}
