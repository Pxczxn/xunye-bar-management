package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.entity.*;
import com.xunye.admin.mapper.*;
import com.xunye.admin.service.CustomerService;
import com.xunye.admin.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final BarTableMapper barTableMapper;
    private final TableAreaMapper tableAreaMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public ShopInfoVO getShopInfo() {
        ShopInfoVO vo = new ShopInfoVO();
        vo.setName("寻野");
        vo.setSlogan("乘兴而去，尽兴而归。");
        vo.setBusinessHours("18:00 - 02:00");
        vo.setNotice("未成年人禁止饮酒，请理性消费。");
        return vo;
    }

    @Override
    public CustomerTableVO getTableByCode(String tableCode) {
        LambdaQueryWrapper<BarTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BarTable::getName, tableCode);
        BarTable table = barTableMapper.selectOne(wrapper);

        if (table == null) {
            throw new BusinessException(404, "桌台不存在");
        }
        if ("DISABLED".equals(table.getStatus())) {
            throw new BusinessException("该桌台已停用");
        }

        CustomerTableVO vo = new CustomerTableVO();
        vo.setId(table.getId());
        vo.setTableCode(table.getName());
        vo.setName(table.getName());
        vo.setStatus(table.getStatus());

        if (table.getAreaId() != null) {
            TableArea area = tableAreaMapper.selectById(table.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getName());
            }
        }

        return vo;
    }

    @Override
    public List<CustomerTableVO> listAvailableTables() {
        LambdaQueryWrapper<BarTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(BarTable::getStatus, "DISABLED")
               .orderByAsc(BarTable::getAreaId)
               .orderByAsc(BarTable::getName);
        List<BarTable> tables = barTableMapper.selectList(wrapper);

        Map<Long, String> areaMap = buildAreaMap(tables);

        return tables.stream().map(t -> {
            CustomerTableVO vo = new CustomerTableVO();
            vo.setId(t.getId());
            vo.setTableCode(t.getName());
            vo.setName(t.getName());
            vo.setStatus(t.getStatus());
            vo.setAreaName(areaMap.getOrDefault(t.getAreaId(), ""));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CustomerCategoryVO> listActiveCategories() {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getStatus, 1)
               .orderByAsc(ProductCategory::getSort);
        List<ProductCategory> categories = categoryMapper.selectList(wrapper);

        return categories.stream().map(c -> {
            CustomerCategoryVO vo = new CustomerCategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CustomerProductVO> listProducts(Long categoryId, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "ON_SALE")
               .gt(Product::getStock, 0);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        List<Product> products = productMapper.selectList(wrapper);

        Map<Long, String> categoryMap = buildCategoryMap(products);
        return products.stream()
                .map(p -> toCustomerProductVO(p, categoryMap))
                .collect(Collectors.toList());
    }

    @Override
    public CustomerProductVO getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!"ON_SALE".equals(product.getStatus())) {
            throw new BusinessException("商品已下架");
        }

        Map<Long, String> categoryMap = buildCategoryMap(Collections.singletonList(product));
        return toCustomerProductVO(product, categoryMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerOrderSubmitVO createOrder(OrderCreateDTO dto) {
        BarTable table = barTableMapper.selectById(dto.getTableId());
        if (table == null) {
            throw new BusinessException(404, "桌台不存在");
        }
        if ("DISABLED".equals(table.getStatus())) {
            throw new BusinessException("该桌台已停用");
        }

        Set<Long> productIds = dto.getItems().stream()
                .map(item -> item.getProductId())
                .collect(Collectors.toSet());

        LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
        pw.in(Product::getId, productIds);
        List<Product> products = productMapper.selectList(pw);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemDTO : dto.getItems()) {
            Product product = productMap.get(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: ID=" + itemDTO.getProductId());
            }
            if (!"ON_SALE".equals(product.getStatus())) {
                throw new BusinessException("商品 [" + product.getName() + "] 已下架");
            }
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException("商品 [" + product.getName() + "] 库存不足，当前库存: " + product.getStock());
            }

            BigDecimal itemAmount = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
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
            Product product = productMap.get(itemDTO.getProductId());
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productMapper.updateById(product);
        }

        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setTableId(table.getId());
        order.setTableName(table.getName());
        order.setTotalAmount(totalAmount);
        order.setStatus("UNPAID");
        order.setServeStatus("PENDING");
        order.setSource("CUSTOMER_MINI");
        order.setRemark(dto.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        orderInfoMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        table.setStatus("USING");
        barTableMapper.updateById(table);

        CustomerOrderSubmitVO vo = new CustomerOrderSubmitVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        return vo;
    }

    @Override
    public OrderPageVO getOrderDetailByOrderNo(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);

        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

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

        LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
        iw.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(iw);

        vo.setItems(items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setPrice(item.getPrice());
            itemVO.setAmount(item.getAmount());
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    private Map<Long, String> buildAreaMap(List<BarTable> tables) {
        Set<Long> areaIds = tables.stream()
                .map(BarTable::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (areaIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<TableArea> aw = new LambdaQueryWrapper<>();
        aw.in(TableArea::getId, areaIds);
        return tableAreaMapper.selectList(aw).stream()
                .collect(Collectors.toMap(TableArea::getId, TableArea::getName));
    }

    private Map<Long, String> buildCategoryMap(List<Product> products) {
        Set<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<ProductCategory> cw = new LambdaQueryWrapper<>();
        cw.in(ProductCategory::getId, categoryIds);
        return categoryMapper.selectList(cw).stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
    }

    private CustomerProductVO toCustomerProductVO(Product p, Map<Long, String> categoryMap) {
        CustomerProductVO vo = new CustomerProductVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setCategoryId(p.getCategoryId());
        vo.setCategoryName(categoryMap.getOrDefault(p.getCategoryId(), ""));
        vo.setPrice(p.getPrice());
        vo.setSpec(p.getSpec());
        vo.setUnit(p.getUnit());
        vo.setStock(p.getStock());
        vo.setDescription(p.getDescription());
        return vo;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "XYO" + timestamp + random;
    }

}
