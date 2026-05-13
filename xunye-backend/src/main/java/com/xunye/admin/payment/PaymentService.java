package com.xunye.admin.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.entity.OrderInfo;
import com.xunye.admin.entity.PaymentOrder;
import com.xunye.admin.mapper.OrderInfoMapper;
import com.xunye.admin.mapper.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProvider paymentProvider;
    private final PaymentOrderMapper paymentOrderMapper;
    private final OrderInfoMapper orderInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    public PaymentOrder createPayment(String orderNo) {
        OrderInfo order = getOrder(orderNo);
        if (!"UNPAID".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许发起支付");
        }

        PaymentOrder existing = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderNo, orderNo)
                        .in(PaymentOrder::getStatus, "PENDING", "SUCCESS")
                        .orderByDesc(PaymentOrder::getId)
                        .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        PaymentOrder po = new PaymentOrder();
        po.setOrderId(order.getId());
        po.setOrderNo(orderNo);
        po.setAmount(order.getTotalAmount());
        po.setCreatedAt(LocalDateTime.now());

        po = paymentProvider.createPayment(po);
        paymentOrderMapper.insert(po);
        return po;
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(String paymentNo) {
        PaymentOrder po = getPaymentOrder(paymentNo);
        if (!"SUCCESS".equals(po.getStatus())) {
            paymentProvider.confirmPayment(paymentNo);
            po = getPaymentOrder(paymentNo);
        }
        syncOrderAfterPaymentSuccess(po);
    }

    private void syncOrderAfterPaymentSuccess(PaymentOrder po) {
        if (!"SUCCESS".equals(po.getStatus())) {
            throw new BusinessException("支付单尚未支付成功");
        }

        OrderInfo order = getOrder(po.getOrderNo());
        if ("PAID".equals(order.getStatus())) {
            return;
        }
        if (!"UNPAID".equals(order.getStatus())) {
            throw new BusinessException("订单已处理");
        }

        if (po.getPaidAt() == null) po.setPaidAt(LocalDateTime.now());
        paymentOrderMapper.updateById(po);

        order.setStatus("PAID");
        order.setPaymentMethod(toBusinessPaymentMethod(po.getProvider()));
        order.setPaidAt(po.getPaidAt());
        orderInfoMapper.updateById(order);
    }

    public PaymentOrder getPaymentOrder(String paymentNo) {
        PaymentOrder po = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getPaymentNo, paymentNo));
        if (po == null) throw new BusinessException(404, "支付单不存在");
        return po;
    }

    private OrderInfo getOrder(String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) throw new BusinessException(404, "订单不存在");
        return order;
    }

    private String toBusinessPaymentMethod(String provider) {
        if ("MOCK".equals(provider) || "WECHAT".equals(provider)) {
            return "WECHAT";
        }
        return provider;
    }
}
