package com.xunye.admin.service.impl;

import com.xunye.admin.service.OrderService;
import com.xunye.admin.vo.OrderRecentVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 订单 Service 实现类（第一阶段：返回固定演示数据）
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public List<OrderRecentVO> getRecentOrders() {
        return Arrays.asList(
                new OrderRecentVO("ORD20260502001", "A01", new BigDecimal("156.00"), "微信", "PAID", "2026-05-02 14:32:15"),
                new OrderRecentVO("ORD20260502002", "B03", new BigDecimal("240.00"), "支付宝", "PAID", "2026-05-02 14:28:42"),
                new OrderRecentVO("ORD20260502003", "C05", new BigDecimal("89.00"), "现金", "UNPAID", "2026-05-02 14:15:33"),
                new OrderRecentVO("ORD20260502004", "A02", new BigDecimal("320.00"), "微信", "PAID", "2026-05-02 13:58:21"),
                new OrderRecentVO("ORD20260502005", "D01", new BigDecimal("175.00"), "支付宝", "CANCELLED", "2026-05-02 13:42:08"),
                new OrderRecentVO("ORD20260502006", "B01", new BigDecimal("420.00"), "微信", "PAID", "2026-05-02 13:30:55"),
                new OrderRecentVO("ORD20260502007", "A04", new BigDecimal("68.00"), "现金", "PAID", "2026-05-02 13:15:12"),
                new OrderRecentVO("ORD20260502008", "C02", new BigDecimal("285.00"), "微信", "UNPAID", "2026-05-02 12:58:47")
        );
    }

}
