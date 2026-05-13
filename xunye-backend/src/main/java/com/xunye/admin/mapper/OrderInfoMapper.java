package com.xunye.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunye.admin.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 统计最近7天销售趋势（仅PAID订单）
     */
    List<Map<String, Object>> selectSalesTrend(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计各支付方式金额和数量（仅PAID订单）
     */
    List<Map<String, Object>> selectPaymentMethodStats();

}
