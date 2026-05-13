package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.entity.OrderInfo;
import com.xunye.admin.entity.Product;
import com.xunye.admin.mapper.OrderInfoMapper;
import com.xunye.admin.mapper.OrderItemMapper;
import com.xunye.admin.mapper.ProductMapper;
import com.xunye.admin.service.DashboardService;
import com.xunye.admin.vo.DashboardSummaryVO;
import com.xunye.admin.vo.HotProductVO;
import com.xunye.admin.vo.PaymentMethodVO;
import com.xunye.admin.vo.SalesTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Map<String, String> PAYMENT_METHOD_TEXT = Map.of(
            "WECHAT", "微信",
            "ALIPAY", "支付宝",
            "CASH", "现金"
    );

    @Override
    public DashboardSummaryVO getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        LambdaQueryWrapper<OrderInfo> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.in(OrderInfo::getStatus, "PAID", "FINISHED")
                .eq(OrderInfo::getDeleted, 0)
                .isNotNull(OrderInfo::getPaidAt)
                .ge(OrderInfo::getPaidAt, todayStart)
                .le(OrderInfo::getPaidAt, todayEnd);
        List<OrderInfo> paidOrders = orderInfoMapper.selectList(paidWrapper);
        BigDecimal todayRevenue = paidOrders.stream()
                .map(OrderInfo::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int paidCount = paidOrders.size();

        LambdaQueryWrapper<OrderInfo> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(OrderInfo::getDeleted, 0)
                .ge(OrderInfo::getCreatedAt, todayStart)
                .le(OrderInfo::getCreatedAt, todayEnd);
        Long todayOrderCount = orderInfoMapper.selectCount(countWrapper);

        BigDecimal avgValue = paidCount > 0
                ? todayRevenue.divide(BigDecimal.valueOf(paidCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LambdaQueryWrapper<Product> warningWrapper = new LambdaQueryWrapper<>();
        warningWrapper.eq(Product::getDeleted, 0)
                .gt(Product::getSafeStock, 0)
                .apply("stock < safe_stock");
        Long warningCount = productMapper.selectCount(warningWrapper);

        DashboardSummaryVO vo = new DashboardSummaryVO();
        vo.setTodayRevenue(todayRevenue);
        vo.setTodayOrderCount(todayOrderCount.intValue());
        vo.setAverageOrderValue(avgValue);
        vo.setInventoryWarningCount(warningCount.intValue());
        return vo;
    }

    @Override
    public List<SalesTrendVO> getSalesTrend() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        List<Map<String, Object>> dbResults = orderInfoMapper.selectSalesTrend(
                startDate.format(DATE_FORMATTER),
                today.format(DATE_FORMATTER)
        );

        Map<String, Map<String, Object>> dbMap = dbResults.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("date"),
                        m -> m,
                        (a, b) -> a
                ));

        List<SalesTrendVO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("MM-dd"));
            Map<String, Object> data = dbMap.get(dateStr);
            BigDecimal revenue = BigDecimal.ZERO;
            int orderCount = 0;
            if (data != null) {
                Object revenueObj = data.get("revenue");
                if (revenueObj != null) {
                    revenue = new BigDecimal(revenueObj.toString());
                }
                Object countObj = data.get("orderCount");
                if (countObj != null) {
                    orderCount = ((Number) countObj).intValue();
                }
            }
            result.add(new SalesTrendVO(dateStr, revenue, orderCount));
        }
        return result;
    }

    @Override
    public List<HotProductVO> getHotProducts() {
        return orderItemMapper.selectHotProducts();
    }

    @Override
    public List<PaymentMethodVO> getPaymentMethods() {
        List<Map<String, Object>> dbResults = orderInfoMapper.selectPaymentMethodStats();
        if (dbResults.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal totalAmount = dbResults.stream()
                .map(m -> {
                    Object obj = m.get("amount");
                    return obj != null ? new BigDecimal(obj.toString()) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return dbResults.stream().map(m -> {
            String method = (String) m.get("paymentMethod");
            Object amountObj = m.get("amount");
            BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;
            double percent = totalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100))
                            .divide(totalAmount, 1, RoundingMode.HALF_UP)
                            .doubleValue()
                    : 0.0;
            String methodName = PAYMENT_METHOD_TEXT.getOrDefault(method, method);
            return new PaymentMethodVO(methodName, amount, percent);
        }).collect(Collectors.toList());
    }

}
