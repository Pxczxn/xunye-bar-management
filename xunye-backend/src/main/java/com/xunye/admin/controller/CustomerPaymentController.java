package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RateLimited;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.entity.PaymentOrder;
import com.xunye.admin.payment.PaymentService;
import com.xunye.admin.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderNo}/payments")
    @AuditLog(operation = "创建支付单", module = "支付管理")
    @RateLimited(limit = 10, period = 60, key = "create_payment")
    public ApiResponse<PaymentVO> createPayment(@PathVariable String orderNo) {
        return ApiResponse.success(toVO(paymentService.createPayment(orderNo)));
    }

    @PostMapping("/payments/{paymentNo}/confirm")
    @AuditLog(operation = "确认支付", module = "支付管理")
    @RateLimited(limit = 10, period = 60, key = "confirm_payment")
    public ApiResponse<Void> confirm(@PathVariable String paymentNo) {
        paymentService.confirmPayment(paymentNo);
        return ApiResponse.success();
    }

    @GetMapping("/payments/{paymentNo}")
    @RateLimited(limit = 50, period = 60)
    public ApiResponse<PaymentVO> query(@PathVariable String paymentNo) {
        return ApiResponse.success(toVO(paymentService.getPaymentOrder(paymentNo)));
    }

    private PaymentVO toVO(PaymentOrder po) {
        PaymentVO vo = new PaymentVO();
        vo.setPaymentNo(po.getPaymentNo());
        vo.setOrderNo(po.getOrderNo());
        vo.setAmount(po.getAmount());
        vo.setProvider(po.getProvider());
        vo.setStatus(po.getStatus());
        return vo;
    }
}
