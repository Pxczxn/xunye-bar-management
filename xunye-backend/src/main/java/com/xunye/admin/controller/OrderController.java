package com.xunye.admin.controller;

import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.service.OrderService;
import com.xunye.admin.vo.OrderRecentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单接口控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取最近订单
     */
    @GetMapping("/recent")
    public ApiResponse<List<OrderRecentVO>> getRecentOrders() {
        return ApiResponse.success(orderService.getRecentOrders());
    }

}
