package com.xunye.admin.service;

import com.xunye.admin.vo.DashboardSummaryVO;
import com.xunye.admin.vo.HotProductVO;
import com.xunye.admin.vo.PaymentMethodVO;
import com.xunye.admin.vo.SalesTrendVO;

import java.util.List;

/**
 * 仪表盘 Service 接口
 */
public interface DashboardService {

    /**
     * 获取概要数据
     */
    DashboardSummaryVO getSummary();

    /**
     * 获取销售趋势（最近7天）
     */
    List<SalesTrendVO> getSalesTrend();

    /**
     * 获取热销商品排行
     */
    List<HotProductVO> getHotProducts();

    /**
     * 获取支付方式占比
     */
    List<PaymentMethodVO> getPaymentMethods();

}
