package com.xunye.admin.controller;

import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.service.CustomerService;
import com.xunye.admin.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/shop/info")
    public ApiResponse<ShopInfoVO> getShopInfo() {
        return ApiResponse.success(customerService.getShopInfo());
    }

    @GetMapping("/tables")
    public ApiResponse<List<CustomerTableVO>> listTables() {
        return ApiResponse.success(customerService.listAvailableTables());
    }

    @GetMapping("/tables/{tableCode}")
    public ApiResponse<CustomerTableVO> getTable(@PathVariable String tableCode) {
        return ApiResponse.success(customerService.getTableByCode(tableCode));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CustomerCategoryVO>> listCategories() {
        return ApiResponse.success(customerService.listActiveCategories());
    }

    @GetMapping("/products")
    public ApiResponse<List<CustomerProductVO>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(customerService.listProducts(categoryId, keyword));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<CustomerProductVO> getProductDetail(@PathVariable Long id) {
        return ApiResponse.success(customerService.getProductDetail(id));
    }

    @PostMapping("/orders")
    public ApiResponse<CustomerOrderSubmitVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return ApiResponse.success(customerService.createOrder(dto));
    }

    @GetMapping("/orders")
    public ApiResponse<List<OrderPageVO>> listOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean all) {
        return ApiResponse.success(customerService.listOrders(page, size, status, date, startDate, endDate, all));
    }

    @GetMapping("/orders/date-markers")
    public ApiResponse<List<OrderDateMarkerVO>> listOrderDateMarkers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        return ApiResponse.success(customerService.listOrderDateMarkers(month));
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<OrderPageVO> getOrderDetail(@PathVariable String orderNo) {
        return ApiResponse.success(customerService.getOrderDetailByOrderNo(orderNo));
    }

    @GetMapping("/messages")
    public ApiResponse<List<CustomerMessageVO>> listMessages(@RequestParam(required = false) String phone) {
        return ApiResponse.success(customerService.listMessages(phone));
    }

    @GetMapping("/stats")
    public ApiResponse<CustomerStatsVO> getStats(@RequestParam(required = false) String phone) {
        return ApiResponse.success(customerService.getCustomerStats(phone));
    }

    @GetMapping("/member/info")
    public ApiResponse<CustomerInfoVO> getMemberInfo(@RequestParam(required = false) String phone) {
        return ApiResponse.success(customerService.getCustomerMemberInfo(phone));
    }

    @GetMapping("/member/levels")
    public ApiResponse<List<MemberLevelVO>> getMemberLevels() {
        return ApiResponse.success(customerService.listMemberLevels());
    }

    @GetMapping("/activities")
    public ApiResponse<List<ActivityVO>> listActiveActivities() {
        return ApiResponse.success(customerService.listActiveActivities());
    }

}
