package com.xunye.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunye.admin.entity.OrderItem;
import com.xunye.admin.vo.HotProductVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单项 Mapper 接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 统计热销商品TOP5（仅PAID订单）
     */
    List<HotProductVO> selectHotProducts();

}
