package com.xunye.admin.service;

import com.xunye.admin.vo.OrderRecentVO;

import java.util.List;

/**
 * 订单 Service 接口
 */
public interface OrderService {

    /**
     * 获取最近订单
     */
    List<OrderRecentVO> getRecentOrders();

}
