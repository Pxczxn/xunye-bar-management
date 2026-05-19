package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.dto.OrderPayDTO;
import com.xunye.admin.dto.OrderQueryDTO;
import com.xunye.admin.entity.BarTable;
import com.xunye.admin.entity.OrderInfo;
import com.xunye.admin.entity.OrderItem;
import com.xunye.admin.entity.Product;
import com.xunye.admin.enums.*;
import com.xunye.admin.mapper.BarTableMapper;
import com.xunye.admin.mapper.OrderInfoMapper;
import com.xunye.admin.mapper.OrderItemMapper;
import com.xunye.admin.mapper.ProductMapper;
import com.xunye.admin.service.OrderService;
import com.xunye.admin.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final BarTableMapper barTableMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<OrderRecentVO> getRecentOrders() {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OrderInfo::getCreatedAt)
               .last("LIMIT 8");

        List<OrderInfo> orders = orderInfoMapper.selectList(wrapper);

        return orders.stream().map(order -> {
            OrderRecentVO vo = new OrderRecentVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setTableName(order.getTableName());
            vo.setAmount(order.getTotalAmount());
            vo.setPaymentMethod(formatPaymentMethod(order.getPaymentMethod()));
            vo.setStatus(order.getStatus());
            vo.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().format(FORMATTER) : "");
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO dto) {
        BarTable table = barTableMapper.selectById(dto.getTableId());
        if (table == null) {
            throw new BusinessException(404, "桌台不存在");
        }

        Set<Long> productIds = dto.getItems().stream()
                .map(item -> item.getProductId())
                .collect(Collectors.toSet());

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getId, productIds);
        List<Product> products = productMapper.selectList(productWrapper);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemDTO : dto.getItems()) {
            Product product = productMap.get(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: ID=" + itemDTO.getProductId());
            }

            BigDecimal itemAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(itemAmount);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setAmount(itemAmount);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        for (var itemDTO : dto.getItems()) {
            int updated = productMapper.decreaseStock(
                itemDTO.getProductId(), 
                itemDTO.getQuantity()
            );
            if (updated == 0) {
                Product product = productMapper.selectById(itemDTO.getProductId());
                throw new BusinessException("商品 [" + product.getName() + "] 库存不足");
            }
        }

        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setTableId(table.getId());
        order.setTableName(table.getName());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.UNPAID.getCode());
        order.setServeStatus(ServeStatus.PENDING.getCode());
        order.setSource(OrderSource.ADMIN_POS.getCode());
        order.setRemark(dto.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        orderInfoMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        table.setStatus(TableStatus.USING.getCode());
        barTableMapper.updateById(table);

        return order.getId();
    }

    @Override
    public PageResult<OrderPageVO> getOrderPage(OrderQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.like(OrderInfo::getOrderNo, queryDTO.getOrderNo());
        }
        if (StringUtils.hasText(queryDTO.getTableName())) {
            wrapper.like(OrderInfo::getTableName, queryDTO.getTableName());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(OrderInfo::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getServeStatus())) {
            wrapper.eq(OrderInfo::getServeStatus, queryDTO.getServeStatus());
        }
        if (StringUtils.hasText(queryDTO.getSource())) {
            wrapper.eq(OrderInfo::getSource, queryDTO.getSource());
        }
        if (StringUtils.hasText(queryDTO.getExcludeStatus())) {
            wrapper.ne(OrderInfo::getStatus, queryDTO.getExcludeStatus());
        }

        wrapper.orderByDesc(OrderInfo::getCreatedAt);

        Page<OrderInfo> pageResult = orderInfoMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<OrderInfo> orders = pageResult.getRecords();
        long total = pageResult.getTotal();

        List<Long> orderIds = orders.stream().map(OrderInfo::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemsMap = Map.of();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> allItems = orderItemMapper.selectList(itemWrapper);
            itemsMap = allItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        }

        Map<Long, List<OrderItem>> finalItemsMap = itemsMap;
        List<OrderPageVO> voList = orders.stream().map(order -> {
            OrderPageVO vo = toOrderPageVO(order);
            List<OrderItem> items = finalItemsMap.getOrDefault(order.getId(), Collections.emptyList());
            vo.setItems(toOrderItemVOList(items));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public OrderPageVO getOrderDetail(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderPageVO vo = toOrderPageVO(order);

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        vo.setItems(toOrderItemVOList(items));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id, OrderPayDTO dto) {
        int updated = orderInfoMapper.payOrderConditional(id,
                OrderStatus.UNPAID.getCode(), OrderStatus.PAID.getCode(),
                dto.getPaymentMethod(), LocalDateTime.now());
        if (updated == 0) {
            if (orderInfoMapper.selectById(id) == null) {
                throw new BusinessException(404, "订单不存在");
            }
            throw new BusinessException("当前订单状态不允许支付，仅未支付订单可支付");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        int updated = orderInfoMapper.cancelOrderConditional(id,
                OrderStatus.UNPAID.getCode(), OrderStatus.CANCELLED.getCode(), LocalDateTime.now());
        if (updated == 0) {
            throw new BusinessException("当前订单状态不允许取消，仅未支付订单可取消");
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        for (OrderItem item : items) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }

        checkAndResetTableStatus(order.getTableId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startMaking(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (OrderStatus.CANCELLED.getCode().equals(order.getStatus())) throw new BusinessException("已取消的订单不能开始制作");
        if (!OrderStatus.PAID.getCode().equals(order.getStatus())) throw new BusinessException("未支付订单不能开始制作");
        if (ServeStatus.FINISHED.getCode().equals(order.getServeStatus())) throw new BusinessException("已完成的订单不能重新制作");
        if (ServeStatus.MAKING.getCode().equals(order.getServeStatus())) throw new BusinessException("订单已在制作中，请勿重复操作");
        order.setServeStatus(ServeStatus.MAKING.getCode());
        orderInfoMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishServe(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (OrderStatus.CANCELLED.getCode().equals(order.getStatus())) throw new BusinessException("已取消的订单不能完成");
        if (!OrderStatus.PAID.getCode().equals(order.getStatus())) throw new BusinessException("未支付订单不能确认制作完成");
        if (!ServeStatus.MAKING.getCode().equals(order.getServeStatus())) throw new BusinessException("只有制作中的订单才能确认完成");
        if (ServeStatus.FINISHED.getCode().equals(order.getServeStatus())) throw new BusinessException("订单已完成，请勿重复操作");
        order.setServeStatus(ServeStatus.FINISHED.getCode());
        orderInfoMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishOrder(Long id) {
        finishServe(id);
    }

    private void checkAndResetTableStatus(Long tableId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getTableId, tableId)
               .eq(OrderInfo::getDeleted, 0)
               .in(OrderInfo::getStatus, OrderStatus.UNPAID.getCode(), OrderStatus.PAID.getCode())
               .ne(OrderInfo::getServeStatus, ServeStatus.FINISHED.getCode());
        long activeCount = orderInfoMapper.selectCount(wrapper);

        if (activeCount == 0) {
            BarTable table = barTableMapper.selectById(tableId);
            if (table != null && TableStatus.USING.getCode().equals(table.getStatus())) {
                table.setStatus(TableStatus.EMPTY.getCode());
                barTableMapper.updateById(table);
            }
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "XYO" + timestamp + random;
    }

    private String formatPaymentMethod(String method) {
        if (method == null) {
            return "";
        }
        try {
            return PaymentMethod.fromCode(method).getDescription();
        } catch (IllegalArgumentException e) {
            return method;
        }
    }

    private OrderPageVO toOrderPageVO(OrderInfo order) {
        OrderPageVO vo = new OrderPageVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTableId(order.getTableId());
        vo.setTableName(order.getTableName());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setServeStatus(order.getServeStatus());
        vo.setPaymentMethod(order.getPaymentMethod());
        vo.setSource(order.getSource());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setPaidAt(order.getPaidAt());
        vo.setCancelledAt(order.getCancelledAt());
        return vo;
    }

    private List<OrderItemVO> toOrderItemVOList(List<OrderItem> items) {
        return items.stream().map(item -> {
            OrderItemVO vo = new OrderItemVO();
            vo.setId(item.getId());
            vo.setProductId(item.getProductId());
            vo.setProductName(item.getProductName());
            vo.setQuantity(item.getQuantity());
            vo.setPrice(item.getPrice());
            vo.setAmount(item.getAmount());
            return vo;
        }).collect(Collectors.toList());
    }

}
