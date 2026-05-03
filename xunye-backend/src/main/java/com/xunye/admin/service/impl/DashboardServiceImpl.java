package com.xunye.admin.service.impl;

import com.xunye.admin.service.DashboardService;
import com.xunye.admin.vo.DashboardSummaryVO;
import com.xunye.admin.vo.HotProductVO;
import com.xunye.admin.vo.PaymentMethodVO;
import com.xunye.admin.vo.SalesTrendVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 仪表盘 Service 实现类（第一阶段：返回固定演示数据）
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public DashboardSummaryVO getSummary() {
        DashboardSummaryVO vo = new DashboardSummaryVO();
        vo.setTodayRevenue(new BigDecimal("3286.00"));
        vo.setTodayOrderCount(42);
        vo.setAverageOrderValue(new BigDecimal("78.24"));
        vo.setInventoryWarningCount(6);
        return vo;
    }

    @Override
    public List<SalesTrendVO> getSalesTrend() {
        return Arrays.asList(
                new SalesTrendVO("04-26", new BigDecimal("2180.00"), 26),
                new SalesTrendVO("04-27", new BigDecimal("2860.00"), 34),
                new SalesTrendVO("04-28", new BigDecimal("2468.00"), 29),
                new SalesTrendVO("04-29", new BigDecimal("3190.00"), 38),
                new SalesTrendVO("04-30", new BigDecimal("4280.00"), 51),
                new SalesTrendVO("05-01", new BigDecimal("3680.00"), 44),
                new SalesTrendVO("05-02", new BigDecimal("3286.00"), 42)
        );
    }

    @Override
    public List<HotProductVO> getHotProducts() {
        return Arrays.asList(
                new HotProductVO("长岛冰茶", 28, new BigDecimal("1680.00")),
                new HotProductVO("百威啤酒", 24, new BigDecimal("720.00")),
                new HotProductVO("莫吉托", 19, new BigDecimal("1140.00")),
                new HotProductVO("野格炸弹", 16, new BigDecimal("960.00")),
                new HotProductVO("威士忌可乐", 13, new BigDecimal("780.00"))
        );
    }

    @Override
    public List<PaymentMethodVO> getPaymentMethods() {
        return Arrays.asList(
                new PaymentMethodVO("微信", new BigDecimal("1800.00"), 54.8),
                new PaymentMethodVO("支付宝", new BigDecimal("986.00"), 30.0),
                new PaymentMethodVO("现金", new BigDecimal("500.00"), 15.2)
        );
    }

}
