package com.xunye.admin.payment;

import com.xunye.admin.entity.PaymentOrder;

public interface PaymentProvider {
    /** 创建支付单，返回填充了 paymentNo/provider 的 PaymentOrder（未持久化）。 */
    PaymentOrder createPayment(PaymentOrder paymentOrder);

    /** 查询支付结果，返回最新 status 和 transactionId。 */
    PaymentOrder queryPayment(String paymentNo);

    /** 确认支付。mock 环境直接确认，真实微信支付由回调或查询结果驱动。 */
    PaymentOrder confirmPayment(String paymentNo);

    /** 关闭支付单。 */
    void closePayment(String paymentNo);
}
