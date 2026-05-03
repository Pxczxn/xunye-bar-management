package com.xunye.admin.controller;

import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.service.DashboardService;
import com.xunye.admin.vo.DashboardSummaryVO;
import com.xunye.admin.vo.HotProductVO;
import com.xunye.admin.vo.PaymentMethodVO;
import com.xunye.admin.vo.SalesTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘接口控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取概要数据
     */
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryVO> getSummary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    /**
     * 获取销售趋势（最近7天）
     */
    @GetMapping("/sales-trend")
    public ApiResponse<List<SalesTrendVO>> getSalesTrend() {
        return ApiResponse.success(dashboardService.getSalesTrend());
    }

    /**
     * 获取热销商品排行
     */
    @GetMapping("/hot-products")
    public ApiResponse<List<HotProductVO>> getHotProducts() {
        return ApiResponse.success(dashboardService.getHotProducts());
    }

    /**
     * 获取支付方式占比
     */
    @GetMapping("/payment-methods")
    public ApiResponse<List<PaymentMethodVO>> getPaymentMethods() {
        return ApiResponse.success(dashboardService.getPaymentMethods());
    }

}
