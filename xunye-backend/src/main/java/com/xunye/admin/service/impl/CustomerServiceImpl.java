package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.entity.*;
import com.xunye.admin.mapper.*;
import com.xunye.admin.service.CustomerService;
import com.xunye.admin.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final BarTableMapper barTableMapper;
    private final TableAreaMapper tableAreaMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final CustomerMapper customerMapper;
    private final CustomerMessageMapper customerMessageMapper;
    private final MemberActivityMapper memberActivityMapper;

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
    public List<OrderPageVO> listOrders(Integer page, Integer size, String status, LocalDate date, LocalDate startDate, LocalDate endDate, Boolean all) {
        int current = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 20 : Math.min(size, 100);
        LocalDate queryStartDate = startDate;
        LocalDate queryEndDate = endDate;

        if (date != null) {
            queryStartDate = date;
            queryEndDate = date;
        }
        if (!Boolean.TRUE.equals(all) && queryStartDate == null && queryEndDate == null) {
            queryStartDate = LocalDate.now();
            queryEndDate = queryStartDate;
        }

        LambdaQueryWrapper<OrderInfo> wrapper = buildCustomerOrderWrapper(status, queryStartDate, queryEndDate);
        wrapper.orderByDesc(OrderInfo::getCreatedAt)
                .last("LIMIT " + ((current - 1) * pageSize) + "," + pageSize);

        List<OrderInfo> orders = orderInfoMapper.selectList(wrapper);
        return attachOrderItems(orders);
    }

    @Override
    public List<OrderDateMarkerVO> listOrderDateMarkers(LocalDate month) {
        LocalDate monthDate = month == null ? LocalDate.now() : month;
        LocalDate startDate = monthDate.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        LambdaQueryWrapper<OrderInfo> wrapper = buildCustomerOrderWrapper(null, startDate, endDate);
        List<OrderInfo> orders = orderInfoMapper.selectList(wrapper);

        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new OrderDateMarkerVO(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    @Override
    public OrderPageVO getOrderDetailByOrderNo(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);

        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderPageVO vo = toOrderPageVO(order);

        LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
        iw.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(iw);

        vo.setItems(items.stream().map(this::toOrderItemVO).collect(Collectors.toList()));

        return vo;
    }

    private LambdaQueryWrapper<OrderInfo> buildCustomerOrderWrapper(String status, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getSource, "CUSTOMER_MINI");
        if (StringUtils.hasText(status)) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(OrderInfo::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(OrderInfo::getCreatedAt, endDate.atTime(LocalTime.MAX));
        }
        return wrapper;
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
        vo.setItems(Collections.emptyList());
        return vo;
    }

    private List<OrderPageVO> attachOrderItems(List<OrderInfo> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream()
                .map(OrderInfo::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, List<OrderItemVO>> itemMap = Collections.emptyMap();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            itemMap = orderItemMapper.selectList(itemWrapper).stream()
                    .collect(Collectors.groupingBy(
                            OrderItem::getOrderId,
                            Collectors.mapping(this::toOrderItemVO, Collectors.toList())
                    ));
        }

        Map<Long, List<OrderItemVO>> finalItemMap = itemMap;
        return orders.stream().map(order -> {
            OrderPageVO vo = toOrderPageVO(order);
            vo.setItems(finalItemMap.getOrDefault(order.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

    private OrderItemVO toOrderItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setQuantity(item.getQuantity());
        vo.setPrice(item.getPrice());
        vo.setAmount(item.getAmount());
        return vo;
    }

    @Override
    public List<CustomerMessageVO> listMessages(String phone) {
        LambdaQueryWrapper<CustomerMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(phone)) {
            wrapper.eq(CustomerMessage::getPhone, phone)
                    .or()
                    .isNull(CustomerMessage::getPhone);
        } else {
            wrapper.isNull(CustomerMessage::getPhone);
        }
        wrapper.orderByDesc(CustomerMessage::getCreatedAt);
        try {
            return customerMessageMapper.selectList(wrapper).stream()
                    .map(this::toCustomerMessageVO)
                    .collect(Collectors.toList());
        } catch (BadSqlGrammarException e) {
            log.warn("customer_message table is not ready, returning empty messages", e);
            return Collections.emptyList();
        }
    }

    @Override
    public CustomerStatsVO getCustomerStats(String phone) {
        CustomerStatsVO vo = new CustomerStatsVO();
        vo.setPoints(BigDecimal.ZERO);
        vo.setCoupons(0);
        vo.setTotalOrders(0);
        vo.setTotalAmount(BigDecimal.ZERO);

        if (!StringUtils.hasText(phone)) {
            return vo;
        }

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getPhone, phone);
        Customer customer = customerMapper.selectOne(wrapper);
        if (customer == null) {
            return vo;
        }

        vo.setPoints(customer.getPoints() == null ? BigDecimal.ZERO : customer.getPoints());
        vo.setTotalOrders(customer.getTotalOrders() == null ? 0 : customer.getTotalOrders());
        vo.setTotalAmount(customer.getTotalAmount() == null ? BigDecimal.ZERO : customer.getTotalAmount());
        return vo;
    }

    @Override
    public CustomerInfoVO getCustomerMemberInfo(String phone) {
        CustomerInfoVO vo = new CustomerInfoVO();
        if (!StringUtils.hasText(phone)) {
            return vo;
        }
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getPhone, phone);
        Customer customer = customerMapper.selectOne(wrapper);
        if (customer == null) {
            return vo;
        }
        vo.setId(customer.getId());
        vo.setPhone(customer.getPhone());
        vo.setNickname(customer.getNickname());
        vo.setAvatar(customer.getAvatar());
        vo.setMemberLevel(customer.getMemberLevel());
        vo.setMemberLevelName(getLevelName(customer.getMemberLevel()));
        vo.setPoints(customer.getPoints() == null ? BigDecimal.ZERO : customer.getPoints());
        vo.setBalance(customer.getBalance() == null ? BigDecimal.ZERO : customer.getBalance());
        vo.setTotalOrders(customer.getTotalOrders() == null ? 0 : customer.getTotalOrders());
        vo.setTotalAmount(customer.getTotalAmount() == null ? BigDecimal.ZERO : customer.getTotalAmount());
        vo.setLastVisitAt(customer.getLastVisitAt());
        vo.setCreatedAt(customer.getCreatedAt());
        return vo;
    }

    @Override
    public List<MemberLevelVO> listMemberLevels() {
        List<MemberLevelVO> levels = new ArrayList<>();

        MemberLevelVO regular = new MemberLevelVO();
        regular.setLevel("REGULAR");
        regular.setName("普通会员");
        regular.setMinAmount(BigDecimal.ZERO);
        regular.setDiscount(new BigDecimal("100"));
        regular.setPointsRate(new BigDecimal("100"));
        regular.setDescription("新注册默认会员等级");
        regular.setSort(1);
        levels.add(regular);

        MemberLevelVO vip = new MemberLevelVO();
        vip.setLevel("VIP");
        vip.setName("VIP会员");
        vip.setMinAmount(new BigDecimal("1000"));
        vip.setDiscount(new BigDecimal("95"));
        vip.setPointsRate(new BigDecimal("150"));
        vip.setDescription("累计消费满1000元自动升级，享95折优惠");
        vip.setSort(2);
        levels.add(vip);

        MemberLevelVO svip = new MemberLevelVO();
        svip.setLevel("SVIP");
        svip.setName("SVIP会员");
        svip.setMinAmount(new BigDecimal("5000"));
        svip.setDiscount(new BigDecimal("90"));
        svip.setPointsRate(new BigDecimal("200"));
        svip.setDescription("累计消费满5000元自动升级，享9折优惠");
        svip.setSort(3);
        levels.add(svip);

        return levels;
    }

    @Override
    public List<ActivityVO> listActiveActivities() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<MemberActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberActivity::getDeleted, 0)
               .eq(MemberActivity::getStatus, 1)
               .le(MemberActivity::getStartDate, now)
               .ge(MemberActivity::getEndDate, now)
               .orderByAsc(MemberActivity::getSort)
               .orderByDesc(MemberActivity::getCreatedAt);
        try {
            return memberActivityMapper.selectList(wrapper).stream()
                    .map(this::toActivityVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("member_activity table not ready, returning empty list", e);
            return Collections.emptyList();
        }
    }

    private ActivityVO toActivityVO(MemberActivity activity) {
        ActivityVO vo = new ActivityVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setType(activity.getType());
        vo.setStartDate(activity.getStartDate());
        vo.setEndDate(activity.getEndDate());
        vo.setCoverImage(activity.getCoverImage());
        vo.setStatus(activity.getStatus());
        vo.setSort(activity.getSort());
        return vo;
    }

    private String getLevelName(String level) {
        if (level == null) return "普通会员";
        return switch (level) {
            case "VIP" -> "VIP会员";
            case "SVIP" -> "SVIP会员";
            default -> "普通会员";
        };
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
        vo.setImageUrl(p.getImageUrl());
        return vo;
    }

    private CustomerMessageVO toCustomerMessageVO(CustomerMessage message) {
        CustomerMessageVO vo = new CustomerMessageVO();
        vo.setId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setRelatedOrderId(message.getRelatedOrderId());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "XYO" + timestamp + random;
    }

}
