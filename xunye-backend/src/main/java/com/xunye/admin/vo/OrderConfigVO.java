package com.xunye.admin.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderConfigVO {

    private List<String> paymentMethods;

    private ReceiptConfig receiptBar;

    private ReceiptConfig receiptKitchen;

    private String cancelTimeout;

    @Data
    public static class ReceiptConfig {
        private Boolean enabled;
        private String printer;
    }

}
