package com.xunye.admin.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.entity.PaymentOrder;
import com.xunye.admin.mapper.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock")
@RequiredArgsConstructor
public class MockPaymentProvider implements PaymentProvider {

    private final PaymentOrderMapper paymentOrderMapper;

    @Override
    public PaymentOrder createPayment(PaymentOrder paymentOrder) {
        String no = "MOCK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
        paymentOrder.setPaymentNo(no);
        paymentOrder.setProvider("MOCK");
        paymentOrder.setStatus("PENDING");
        return paymentOrder;
    }

    @Override
    public PaymentOrder queryPayment(String paymentNo) {
        return getByNo(paymentNo);
    }

    @Override
    public PaymentOrder confirmPayment(String paymentNo) {
        PaymentOrder po = getByNo(paymentNo);
        if (!"PENDING".equals(po.getStatus())) {
            throw new BusinessException("支付单状态不可确认");
        }
        po.setStatus("SUCCESS");
        po.setTransactionId("MOCK_TXN_" + paymentNo);
        po.setPaidAt(LocalDateTime.now());
        paymentOrderMapper.updateById(po);
        return po;
    }

    @Override
    public void closePayment(String paymentNo) {
        PaymentOrder po = getByNo(paymentNo);
        po.setStatus("CLOSED");
        paymentOrderMapper.updateById(po);
    }

    private PaymentOrder getByNo(String paymentNo) {
        PaymentOrder po = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getPaymentNo, paymentNo));
        if (po == null) throw new BusinessException(404, "支付单不存在");
        return po;
    }
}
