package com.xunye.admin.controller;

import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.service.CustomerService;
import com.xunye.admin.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<OrderPageVO> getOrderDetail(@PathVariable String orderNo) {
        return ApiResponse.success(customerService.getOrderDetailByOrderNo(orderNo));
    }

}
