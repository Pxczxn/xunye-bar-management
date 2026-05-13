package com.xunye.admin.service;

import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.vo.*;

import java.util.List;

public interface CustomerService {

    ShopInfoVO getShopInfo();

    CustomerTableVO getTableByCode(String tableCode);

    List<CustomerTableVO> listAvailableTables();

    List<CustomerCategoryVO> listActiveCategories();

    List<CustomerProductVO> listProducts(Long categoryId, String keyword);

    CustomerProductVO getProductDetail(Long id);

    CustomerOrderSubmitVO createOrder(OrderCreateDTO dto);

    OrderPageVO getOrderDetailByOrderNo(String orderNo);

}
