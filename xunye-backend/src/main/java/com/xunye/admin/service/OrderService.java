package com.xunye.admin.service;

import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.dto.OrderPayDTO;
import com.xunye.admin.dto.OrderQueryDTO;
import com.xunye.admin.vo.OrderPageVO;
import com.xunye.admin.vo.OrderRecentVO;
import com.xunye.admin.vo.PageResult;

import java.util.List;

/**
 * 订单 Service 接口
 */
public interface OrderService {

    /**
     * 获取最近订单
     */
    List<OrderRecentVO> getRecentOrders();

    /**
     * 创建订单
     */
    Long createOrder(OrderCreateDTO dto);

    /**
     * 分页查询订单
     */
    PageResult<OrderPageVO> getOrderPage(OrderQueryDTO queryDTO);

    /**
     * 查询订单详情
     */
    OrderPageVO getOrderDetail(Long id);

    /**
     * 支付订单
     */
    void payOrder(Long id, OrderPayDTO dto);

    void startMaking(Long id);

    void finishServe(Long id);

    void finishOrder(Long id);

    /**
     * 取消订单
     */
    void cancelOrder(Long id);

}
