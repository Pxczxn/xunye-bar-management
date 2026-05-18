package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.dto.OrderPayDTO;
import com.xunye.admin.dto.OrderQueryDTO;
import com.xunye.admin.service.OrderService;
import com.xunye.admin.vo.OrderPageVO;
import com.xunye.admin.vo.OrderRecentVO;
import com.xunye.admin.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER", "STAFF"})
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取最近订单
     */
    @GetMapping("/recent")
    public ApiResponse<List<OrderRecentVO>> getRecentOrders() {
        return ApiResponse.success(orderService.getRecentOrders());
    }

    /**
     * 创建订单
     */
    @PostMapping
    @AuditLog(operation = "创建订单", module = "订单管理")
    public ApiResponse<Long> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return ApiResponse.success(orderService.createOrder(dto));
    }

    /**
     * 分页查询订单
     */
    @GetMapping
    public ApiResponse<PageResult<OrderPageVO>> getOrderPage(OrderQueryDTO queryDTO) {
        return ApiResponse.success(orderService.getOrderPage(queryDTO));
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<OrderPageVO> getOrderDetail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderDetail(id));
    }

    /**
     * 支付订单
     */
    @PatchMapping("/{id}/pay")
    @AuditLog(operation = "支付订单", module = "订单管理")
    public ApiResponse<Void> payOrder(@PathVariable Long id, @Valid @RequestBody OrderPayDTO dto) {
        orderService.payOrder(id, dto);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/cancel")
    @AuditLog(operation = "取消订单", module = "订单管理")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/making")
    @AuditLog(operation = "开始制作", module = "订单管理")
    public ApiResponse<Void> startMaking(@PathVariable Long id) {
        orderService.startMaking(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/finish")
    @AuditLog(operation = "完成订单", module = "订单管理")
    public ApiResponse<Void> finishOrder(@PathVariable Long id) {
        orderService.finishOrder(id);
        return ApiResponse.success(null);
    }

}
