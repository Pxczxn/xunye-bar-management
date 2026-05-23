package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.CustomerPhoneLoginDTO;
import com.xunye.admin.dto.CustomerProfileUpdateDTO;
import com.xunye.admin.dto.CustomerSetPasswordDTO;
import com.xunye.admin.dto.CustomerWxLoginDTO;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.entity.*;
import com.xunye.admin.mapper.*;
import com.xunye.admin.service.CustomerService;
import com.xunye.admin.service.SystemConfigService;
import com.xunye.admin.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final Map<String, RegisterCode> REGISTER_CODES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, RegisterCode> LOGIN_CODES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long REGISTER_CODE_TTL_SECONDS = 300;
    private static final long REGISTER_CODE_SEND_INTERVAL_SECONDS = 30;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final BarTableMapper barTableMapper;
    private final TableAreaMapper tableAreaMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final CustomerMapper customerMapper;
    private final CustomerMessageMapper customerMessageMapper;
    private final CustomerCouponMapper customerCouponMapper;
    private final CustomerPointsRecordMapper customerPointsRecordMapper;
    private final MemberActivityMapper memberActivityMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper;

    @Value("${file.upload.base-path}")
    private String fileUploadBasePath;

    @Override
    public ShopInfoVO getShopInfo() {
        ShopConfigVO shopConfig = systemConfigService.getShopConfig();
        MiniappConfigVO miniappConfig = systemConfigService.getMiniappConfig();
        ShopInfoVO vo = new ShopInfoVO();
        vo.setName(shopConfig.getName());
        vo.setSlogan(shopConfig.getSlogan());
        vo.setBusinessHours(shopConfig.getBusinessHours());
        vo.setNotice(shopConfig.getNotice());
        vo.setBannerImages(miniappConfig.getBannerImages());
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
               .gt(Product::getStock, 0)
               .last("LIMIT 200");
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
            int rows = productMapper.decreaseStock(product.getId(), itemDTO.getQuantity());
            if (rows == 0) {
                throw new BusinessException("商品 [" + product.getName() + "] 库存扣减失败，请稍后重试");
            }
        }

        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setTableId(table.getId());
        order.setTableName(table.getName());
        order.setCustomerPhone(dto.getPhone());
        Customer customer = ensureCustomer(dto.getPhone());
        if (customer != null) {
            order.setCustomerId(customer.getId());
        }
        BigDecimal discountAmount = resolveDiscount(dto.getPhone(), dto.getCouponId(), totalAmount);
        BigDecimal payableAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        order.setOriginalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setCouponId(dto.getCouponId());
        order.setTotalAmount(payableAmount);
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
        vo.setOriginalAmount(order.getOriginalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
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
        vo.setOriginalAmount(order.getOriginalAmount() == null ? order.getTotalAmount() : order.getOriginalAmount());
        vo.setDiscountAmount(order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount());
        vo.setCouponId(order.getCouponId());
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

        LambdaQueryWrapper<CustomerCoupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(CustomerCoupon::getPhone, phone)
                     .eq(CustomerCoupon::getUsed, 0)
                     .gt(CustomerCoupon::getValidUntil, java.time.LocalDate.now());
        Long couponCount = customerCouponMapper.selectCount(couponWrapper);
        vo.setCoupons(couponCount == null ? 0 : couponCount.intValue());

        return vo;
    }

    @Override
    public CustomerInfoVO getCustomerMemberInfo(String phone) {
        if (!StringUtils.hasText(phone)) {
            return new CustomerInfoVO();
        }
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getPhone, phone);
        Customer customer = customerMapper.selectOne(wrapper);
        if (customer == null) {
            return new CustomerInfoVO();
        }
        return toCustomerInfoVO(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerInfoVO updateCustomerProfile(CustomerProfileUpdateDTO dto) {
        Customer customer = findCustomer(dto.getPhone(), dto.getCustomerNo());
        if (customer == null) {
            customer = createDefaultCustomer(dto.getPhone());
        } else if (!Objects.equals(customer.getPhone(), dto.getPhone())) {
            ensurePhoneAvailable(dto.getPhone(), customer.getId());
            customer.setPhone(dto.getPhone());
        }

        if (StringUtils.hasText(dto.getNickname())) {
            customer.setNickname(dto.getNickname().trim());
        }
        customer.setAvatar(dto.getAvatar());
        customer.setBirthday(dto.getBirthday());
        customer.setGender(dto.getGender());
        customer.setFavoriteTaste(dto.getFavoriteTaste());
        customer.setFavoriteTable(dto.getFavoriteTable());
        customer.setUpdatedAt(LocalDateTime.now());

        if (customer.getId() == null) {
            customerMapper.insert(customer);
        } else {
            customerMapper.updateById(customer);
        }
        return toCustomerInfoVO(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerInfoVO wxLogin(CustomerWxLoginDTO dto) {
        if (!StringUtils.hasText(dto.getCode())) {
            throw new BusinessException("微信登录 code 不能为空");
        }
        String openid = "mock_openid_" + dto.getCode();
        Customer customer = findCustomer(dto.getPhone(), dto.getCustomerNo());
        if (customer == null) {
            LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Customer::getOpenid, openid);
            customer = customerMapper.selectOne(wrapper);
        }
        if (customer == null) {
            throw new BusinessException(404, "会员不存在");
        }
        if (!StringUtils.hasText(customer.getOpenid())) {
            customer.setOpenid(openid);
        }
        if (StringUtils.hasText(dto.getNickname())) {
            customer.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            customer.setAvatar(dto.getAvatar());
        }
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.updateById(customer);
        return toCustomerInfoVO(customer);
    }

    @Override
    public void sendRegisterCode(String phone) {
        String normalizedPhone = normalizeRegisterPhone(phone);
        ensurePhoneAvailable(normalizedPhone, null);
        sendCode(REGISTER_CODES, normalizedPhone);
    }

    @Override
    public void sendLoginCode(String phone) {
        String normalizedPhone = normalizeRegisterPhone(phone);
        // Ensure customer exists for login
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getPhone, normalizedPhone);
        Customer customer = customerMapper.selectOne(wrapper);
        if (customer == null) {
            throw new BusinessException("该手机号未注册会员");
        }
        sendCode(LOGIN_CODES, normalizedPhone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerInfoVO phoneLoginByCode(CustomerPhoneLoginDTO dto) {
        String phone = normalizeRegisterPhone(dto.getPhone());
        if (!StringUtils.hasText(dto.getVerifyCode())) {
            throw new BusinessException("验证码不能为空");
        }
        verifyCode(LOGIN_CODES, phone, dto.getVerifyCode());
        Customer customer = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, phone));
        if (customer == null) {
            throw new BusinessException(404, "会员不存在");
        }
        LOGIN_CODES.remove(phone);
        return toCustomerInfoVO(customer);
    }

    @Override
    public CustomerInfoVO phoneLoginByPassword(CustomerPhoneLoginDTO dto) {
        String phone = normalizeRegisterPhone(dto.getPhone());
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        Customer customer = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, phone));
        if (customer == null) {
            throw new BusinessException(404, "会员不存在");
        }
        if (!StringUtils.hasText(customer.getPassword())) {
            throw new BusinessException("未设置密码，请使用验证码登录");
        }
        if (!PASSWORD_ENCODER.matches(dto.getPassword(), customer.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return toCustomerInfoVO(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPassword(CustomerSetPasswordDTO dto) {
        String phone = normalizeRegisterPhone(dto.getPhone());
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (dto.getPassword().length() < 6 || dto.getPassword().length() > 64) {
            throw new BusinessException("密码长度6-64位");
        }
        verifyCode(LOGIN_CODES, phone, dto.getVerifyCode());
        Customer customer = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, phone));
        if (customer == null) {
            throw new BusinessException(404, "会员不存在");
        }
        customer.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.updateById(customer);
        LOGIN_CODES.remove(phone);
    }

    private void sendCode(Map<String, RegisterCode> codeMap, String phone) {
        RegisterCode cached = codeMap.get(phone);
        if (cached != null && cached.sentAt().plusSeconds(REGISTER_CODE_SEND_INTERVAL_SECONDS).isAfter(LocalDateTime.now())) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime now = LocalDateTime.now();
        codeMap.put(phone, new RegisterCode(code, now.plusSeconds(REGISTER_CODE_TTL_SECONDS), now));
        log.info("验证码 phone={}, code={}", phone, code);
    }

    private void verifyCode(Map<String, RegisterCode> codeMap, String phone, String code) {
        RegisterCode cached = codeMap.get(phone);
        if (cached == null || cached.expiresAt().isBefore(LocalDateTime.now())) {
            codeMap.remove(phone);
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!Objects.equals(cached.code(), code.trim())) {
            throw new BusinessException("验证码不正确");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerInfoVO registerMember(CustomerWxLoginDTO dto) {
        if (!StringUtils.hasText(dto.getCode())) {
            throw new BusinessException("微信登录 code 不能为空");
        }
        String phone = normalizeRegisterPhone(dto.getPhone());
        verifyRegisterCode(phone, dto.getVerifyCode());
        ensurePhoneAvailable(phone, null);
        String openid = "mock_openid_" + dto.getCode();
        Customer customer = findCustomer(null, dto.getCustomerNo());
        if (customer == null) {
            customer = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                    .eq(Customer::getOpenid, openid));
        }
        if (customer == null) {
            customer = createDefaultCustomer(phone);
            customer.setOpenid(openid);
            customerMapper.insert(customer);
        }
        else {
            ensurePhoneAvailable(phone, customer.getId());
            customer.setPhone(phone);
            if (!StringUtils.hasText(customer.getOpenid())) {
                customer.setOpenid(openid);
            }
        }
        if (StringUtils.hasText(dto.getNickname())) {
            customer.setNickname(dto.getNickname().trim());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            customer.setAvatar(dto.getAvatar());
        }
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.updateById(customer);
        REGISTER_CODES.remove(phone);
        return toCustomerInfoVO(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(String phone, String customerNo, MultipartFile file) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("头像不能超过5MB");
        }
        try {
            String extension = resolveExtension(file.getOriginalFilename(), contentType);
            String filename = UUID.randomUUID() + extension;
            Path uploadPath = Paths.get(fileUploadBasePath, "avatars");
            Files.createDirectories(uploadPath);
            file.transferTo(uploadPath.resolve(filename).toFile());
            String avatarUrl = "/images/avatars/" + filename;
            Customer customer = findCustomer(phone, customerNo);
            if (customer == null) {
                customer = ensureCustomer(phone);
            }
            customer.setAvatar(avatarUrl);
            customer.setUpdatedAt(LocalDateTime.now());
            customerMapper.updateById(customer);
            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessException("头像上传失败");
        }
    }

    @Override
    public List<CustomerCouponVO> listCoupons(String phone) {
        if (!StringUtils.hasText(phone)) {
            return Collections.emptyList();
        }
        ensureDefaultCoupons(phone);
        LambdaQueryWrapper<CustomerCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerCoupon::getPhone, phone)
                .orderByAsc(CustomerCoupon::getUsed)
                .orderByDesc(CustomerCoupon::getCreatedAt);
        return customerCouponMapper.selectList(wrapper).stream()
                .map(this::toCustomerCouponVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerCouponVO exchangePointsForCoupon(String phone, Long rewardId) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        Customer customer = ensureCustomer(phone);
        RewardDefinition reward = getReward(rewardId);
        int points = customer.getPoints() == null ? 0 : customer.getPoints().intValue();
        if (points < reward.cost()) {
            throw new BusinessException("积分不足");
        }
        customer.setPoints(BigDecimal.valueOf(points - reward.cost()));
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.updateById(customer);

        CustomerCoupon coupon = createCoupon(phone, reward.title(), reward.rule(), reward.discountAmount(), reward.minAmount(), LocalDate.now().plusDays(45));
        customerCouponMapper.insert(coupon);
        insertPointsRecord(phone, "兑换优惠券", -reward.cost(), null);
        return toCustomerCouponVO(coupon);
    }

    @Override
    public List<CustomerPointsRecordVO> listPointsRecords(String phone) {
        if (!StringUtils.hasText(phone)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<CustomerPointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerPointsRecord::getPhone, phone)
                .orderByDesc(CustomerPointsRecord::getCreatedAt)
                .last("LIMIT 50");
        return customerPointsRecordMapper.selectList(wrapper).stream()
                .map(this::toCustomerPointsRecordVO)
                .collect(Collectors.toList());
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
        Map<String, Object> settings = readActivitySettings(activity.getSettings());
        ActivityVO vo = new ActivityVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setType(activity.getType());
        vo.setStartDate(activity.getStartDate());
        vo.setEndDate(activity.getEndDate());
        vo.setCoverImage(activity.getCoverImage());
        vo.setSettings(settings);
        vo.setSettingSummary(ActivitySettingsHelper.summarize(activity.getType(), settings));
        vo.setStatus(activity.getStatus());
        vo.setSort(activity.getSort());
        return vo;
    }

    private Map<String, Object> readActivitySettings(String settingsJson) {
        if (!StringUtils.hasText(settingsJson)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(settingsJson, MAP_TYPE);
        } catch (JsonProcessingException e) {
            log.warn("failed to parse activity settings: {}", settingsJson, e);
            return Collections.emptyMap();
        }
    }

    private String getLevelName(String level) {
        if (level == null) return "普通会员";
        return switch (level) {
            case "VIP" -> "VIP会员";
            case "SVIP" -> "SVIP会员";
            default -> "普通会员";
        };
    }

    private Customer createDefaultCustomer(String phone) {
        Customer customer = new Customer();
        customer.setCustomerNo(generateCustomerNo());
        customer.setPhone(phone);
        customer.setNickname("寻野会员");
        customer.setMemberLevel("REGULAR");
        customer.setPoints(BigDecimal.ZERO);
        customer.setBalance(BigDecimal.ZERO);
        customer.setTotalOrders(0);
        customer.setTotalAmount(BigDecimal.ZERO);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    private String generateCustomerNo() {
        return "XY" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", new Random().nextInt(10000));
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        if ("image/png".equals(contentType)) return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        return ".jpg";
    }

    private Customer ensureCustomer(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getPhone, phone);
        Customer customer = customerMapper.selectOne(wrapper);
        if (customer != null) {
            return customer;
        }
        Customer created = createDefaultCustomer(phone);
        customerMapper.insert(created);
        return created;
    }

    private Customer findCustomer(String phone, String customerNo) {
        if (StringUtils.hasText(customerNo)) {
            Customer byNo = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                    .eq(Customer::getCustomerNo, customerNo));
            if (byNo != null) {
                return byNo;
            }
        }
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, phone));
    }

    private void ensurePhoneAvailable(String phone, Long currentCustomerId) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        Customer exists = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, phone));
        if (exists != null && !Objects.equals(exists.getId(), currentCustomerId)) {
            throw new BusinessException("该手机号已绑定其他顾客");
        }
    }

    private String normalizeRegisterPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        String normalizedPhone = phone.trim();
        if (!normalizedPhone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        return normalizedPhone;
    }

    private void verifyRegisterCode(String phone, String verifyCode) {
        if (!StringUtils.hasText(verifyCode)) {
            throw new BusinessException("验证码不能为空");
        }
        RegisterCode cached = REGISTER_CODES.get(phone);
        if (cached == null || cached.expiresAt().isBefore(LocalDateTime.now())) {
            REGISTER_CODES.remove(phone);
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!Objects.equals(cached.code(), verifyCode.trim())) {
            throw new BusinessException("验证码不正确");
        }
    }

    private record RegisterCode(String code, LocalDateTime expiresAt, LocalDateTime sentAt) {
    }

    private BigDecimal resolveDiscount(String phone, Long couponId, BigDecimal totalAmount) {
        if (couponId == null) {
            return BigDecimal.ZERO;
        }
        CustomerCoupon coupon = customerCouponMapper.selectById(couponId);
        if (coupon == null || !Objects.equals(coupon.getPhone(), phone)) {
            throw new BusinessException("优惠券不存在");
        }
        if (coupon.getUsed() != null && coupon.getUsed() == 1) {
            throw new BusinessException("优惠券已使用");
        }
        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDate.now())) {
            throw new BusinessException("优惠券已过期");
        }
        if (coupon.getMinAmount() != null && totalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("未达到优惠券使用门槛");
        }
        coupon.setUsed(1);
        coupon.setUsedAt(LocalDateTime.now());
        customerCouponMapper.updateById(coupon);
        return coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount().min(totalAmount);
    }

    private void ensureDefaultCoupons(String phone) {
        Long count = customerCouponMapper.selectCount(new LambdaQueryWrapper<CustomerCoupon>().eq(CustomerCoupon::getPhone, phone));
        if (count != null && count > 0) {
            return;
        }
        customerCouponMapper.insert(createCoupon(phone, "满 99 减 10", "全场酒水可用", new BigDecimal("10"), new BigDecimal("99"), LocalDate.now().plusDays(40)));
        customerCouponMapper.insert(createCoupon(phone, "小食立减 18", "佐酒小食可用", new BigDecimal("18"), BigDecimal.ZERO, LocalDate.now().plusDays(30)));
        customerCouponMapper.insert(createCoupon(phone, "特调第二杯半价", "招牌特调可用", new BigDecimal("34"), new BigDecimal("68"), LocalDate.now().plusDays(20)));
    }

    private CustomerCoupon createCoupon(String phone, String title, String rule, BigDecimal discountAmount, BigDecimal minAmount, LocalDate validUntil) {
        CustomerCoupon coupon = new CustomerCoupon();
        coupon.setPhone(phone);
        coupon.setTitle(title);
        coupon.setRuleText(rule);
        coupon.setDiscountAmount(discountAmount);
        coupon.setMinAmount(minAmount);
        coupon.setValidUntil(validUntil);
        coupon.setUsed(0);
        coupon.setCreatedAt(LocalDateTime.now());
        return coupon;
    }

    private CustomerCouponVO toCustomerCouponVO(CustomerCoupon coupon) {
        CustomerCouponVO vo = new CustomerCouponVO();
        vo.setId(coupon.getId());
        vo.setTitle(coupon.getTitle());
        vo.setRule(coupon.getRuleText());
        vo.setDiscountAmount(coupon.getDiscountAmount());
        vo.setMinAmount(coupon.getMinAmount());
        vo.setUsed(coupon.getUsed() != null && coupon.getUsed() == 1);
        vo.setValidUntil(coupon.getValidUntil());
        return vo;
    }

    private CustomerPointsRecordVO toCustomerPointsRecordVO(CustomerPointsRecord record) {
        CustomerPointsRecordVO vo = new CustomerPointsRecordVO();
        vo.setId(record.getId());
        vo.setTitle(record.getTitle());
        vo.setAmount(record.getAmount());
        vo.setRelatedOrderNo(record.getRelatedOrderNo());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    private void insertPointsRecord(String phone, String title, Integer amount, String orderNo) {
        if (!StringUtils.hasText(phone) || amount == null || amount == 0) {
            return;
        }
        CustomerPointsRecord record = new CustomerPointsRecord();
        record.setPhone(phone);
        record.setTitle(title);
        record.setAmount(amount);
        record.setRelatedOrderNo(orderNo);
        record.setCreatedAt(LocalDateTime.now());
        customerPointsRecordMapper.insert(record);
    }

    private RewardDefinition getReward(Long rewardId) {
        if (Objects.equals(rewardId, 2L)) {
            return new RewardDefinition(2L, "小食抵扣券", "任选小食立减 18", 120, new BigDecimal("18"), BigDecimal.ZERO);
        }
        return new RewardDefinition(1L, "满 99 减 10", "下次到店消费可用", 80, new BigDecimal("10"), new BigDecimal("99"));
    }

    private record RewardDefinition(Long id, String title, String rule, int cost, BigDecimal discountAmount, BigDecimal minAmount) {
    }

    private CustomerInfoVO toCustomerInfoVO(Customer customer) {
        CustomerInfoVO vo = new CustomerInfoVO();
        vo.setId(customer.getId());
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setOpenid(customer.getOpenid());
        vo.setPhone(customer.getPhone());
        vo.setNickname(customer.getNickname());
        vo.setAvatar(customer.getAvatar());
        vo.setBirthday(customer.getBirthday());
        vo.setGender(customer.getGender());
        vo.setFavoriteTaste(customer.getFavoriteTaste());
        vo.setFavoriteTable(customer.getFavoriteTable());
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
