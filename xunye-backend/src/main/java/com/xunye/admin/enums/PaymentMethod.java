package com.xunye.admin.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    WECHAT("WECHAT", "微信"),
    ALIPAY("ALIPAY", "支付宝"),
    CASH("CASH", "现金");

    private final String code;
    private final String description;

    PaymentMethod(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知的支付方式: " + code);
    }
}
