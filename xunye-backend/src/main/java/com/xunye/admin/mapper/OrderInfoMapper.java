package com.xunye.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunye.admin.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 统计今日看板汇总，避免把订单明细全部拉回应用层。
     */
    Map<String, Object> selectTodaySummary(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计最近7天销售趋势（仅PAID订单）
     */
    List<Map<String, Object>> selectSalesTrend(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计各支付方式金额和数量（仅PAID订单）
     */
    List<Map<String, Object>> selectPaymentMethodStats();

    @Update("UPDATE order_info SET status=#{newStatus}, payment_method=#{paymentMethod}, paid_at=#{paidAt} WHERE id=#{id} AND status=#{expectedStatus} AND deleted=0")
    int payOrderConditional(@Param("id") Long id, @Param("expectedStatus") String expectedStatus,
                            @Param("newStatus") String newStatus, @Param("paymentMethod") String paymentMethod,
                            @Param("paidAt") LocalDateTime paidAt);

    @Update("UPDATE order_info SET status=#{newStatus}, cancelled_at=#{cancelledAt} WHERE id=#{id} AND status=#{expectedStatus} AND deleted=0")
    int cancelOrderConditional(@Param("id") Long id, @Param("expectedStatus") String expectedStatus,
                               @Param("newStatus") String newStatus, @Param("cancelledAt") LocalDateTime cancelledAt);

}
