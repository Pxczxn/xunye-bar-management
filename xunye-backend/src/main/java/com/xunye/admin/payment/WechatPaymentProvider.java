package com.xunye.admin.payment;

import com.xunye.admin.entity.PaymentOrder;

/**
 * TODO: 接入微信支付。
 * 1. 实现 createPayment，返回 wx.requestPayment 所需参数。
 * 2. 实现 queryPayment，用于主动查询微信支付结果。
 * 3. 支付成功以微信回调或查询结果为准，不从前端直接确认真实支付。
 */
public class WechatPaymentProvider implements PaymentProvider {

    @Override
    public PaymentOrder createPayment(PaymentOrder paymentOrder) {
        throw new UnsupportedOperationException("微信支付尚未实现");
    }

    @Override
    public PaymentOrder queryPayment(String paymentNo) {
        throw new UnsupportedOperationException("微信支付尚未实现");
    }

    @Override
    public PaymentOrder confirmPayment(String paymentNo) {
        throw new UnsupportedOperationException("微信支付确认应由支付回调或查询结果驱动");
    }

    @Override
    public void closePayment(String paymentNo) {
        throw new UnsupportedOperationException("微信支付尚未实现");
    }
}
