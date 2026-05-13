package com.xunye.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.dto.OrderItemDTO;
import com.xunye.admin.dto.OrderPayDTO;
import com.xunye.admin.entity.OrderInfo;
import com.xunye.admin.entity.Product;
import com.xunye.admin.mapper.OrderInfoMapper;
import com.xunye.admin.mapper.OrderItemMapper;
import com.xunye.admin.mapper.ProductMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    private static final Long PRODUCT_1 = 1L;
    private static final Long PRODUCT_2 = 2L;
    private static final Long PRODUCT_3 = 3L;
    private static final Long PRODUCT_4 = 4L;
    private static final Long PRODUCT_5 = 5L;
    private static final Long TABLE_ID = 4L;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String token;

    private String getToken() throws Exception {
        if (token == null) {
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", "admin");
            loginData.put("password", "123456");
            MvcResult r = mockMvc.perform(post("/api/admin/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData))).andReturn();
            token = objectMapper.readTree(r.getResponse().getContentAsString())
                    .path("data").path("token").asText();
        }
        return token;
    }

    private void resetAllData() {
        jdbcTemplate.update("DELETE FROM order_item");
        jdbcTemplate.update("DELETE FROM order_info");
        jdbcTemplate.update("UPDATE product SET stock = 86 WHERE id = 1");
        jdbcTemplate.update("UPDATE product SET stock = 85 WHERE id = 2");
        jdbcTemplate.update("UPDATE product SET stock = 45 WHERE id = 3");
        jdbcTemplate.update("UPDATE product SET stock = 200 WHERE id = 4");
        jdbcTemplate.update("UPDATE product SET stock = 999 WHERE id = 5");
        jdbcTemplate.update("UPDATE bar_table SET status = 'EMPTY' WHERE id = ?", TABLE_ID);
    }

    private Long createAndPayOrder(Long tableId, Long productId, int quantity, String paymentMethod) throws Exception {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setTableId(tableId);
        dto.setRemark("Dashboard测试");

        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        dto.setItems(List.of(item));

        MvcResult result = mockMvc.perform(post("/api/admin/orders")
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).path("data").asLong();

        if (paymentMethod != null) {
            OrderPayDTO payDTO = new OrderPayDTO();
            payDTO.setPaymentMethod(paymentMethod);

            mockMvc.perform(patch("/api/admin/orders/{id}/pay", orderId)
                    .header("Authorization", "Bearer " + getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
        return orderId;
    }

    private JsonNode fetchDashboard(String endpoint) throws Exception {
        MvcResult result = mockMvc.perform(get(endpoint)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    // ==================== TC-01: 正常数据测试 ====================
    @Test
    @Order(1)
    @DisplayName("TC-01: 正常数据 - 创建多笔不同支付方式订单并验证看板统计")
    public void testNormalData() throws Exception {
        resetAllData();

        createAndPayOrder(TABLE_ID, PRODUCT_1, 2, "WECHAT");   // 60元 微信
        createAndPayOrder(TABLE_ID, PRODUCT_2, 1, "ALIPAY");   // 60元 支付宝
        createAndPayOrder(TABLE_ID, PRODUCT_3, 2, "CASH");     // 120元 现金

        JsonNode summary = fetchDashboard("/api/admin/dashboard/summary");

        BigDecimal revenue = new BigDecimal(summary.path("todayRevenue").asText());
        assertEquals(0, revenue.compareTo(new BigDecimal("240.00")), "今日营收应为240.00");

        assertEquals(3, summary.path("todayOrderCount").asInt(), "今日订单数应为3");

        BigDecimal avgValue = new BigDecimal(summary.path("averageOrderValue").asText());
        assertEquals(0, avgValue.compareTo(new BigDecimal("80.00")), "平均客单价应为80.00");

        JsonNode methods = fetchDashboard("/api/admin/dashboard/payment-methods");
        assertTrue(methods.isArray(), "支付方式应为数组");
        assertEquals(3, methods.size(), "应有3种支付方式");

        BigDecimal totalPercent = BigDecimal.ZERO;
        for (JsonNode m : methods) {
            totalPercent = totalPercent.add(BigDecimal.valueOf(m.path("percent").asDouble()));
            assertTrue(m.has("method"), "应包含method字段");
            assertTrue(m.has("amount"), "应包含amount字段");
            assertTrue(m.has("percent"), "应包含percent字段");
        }
        assertEquals(100.0, totalPercent.doubleValue(), 0.01, "百分比之和应为100%");

        JsonNode hotProducts = fetchDashboard("/api/admin/dashboard/hot-products");
        assertTrue(hotProducts.isArray(), "热销商品应为数组");
        assertTrue(hotProducts.size() <= 5, "热销商品不超过5个");
        if (hotProducts.size() > 0) {
            JsonNode first = hotProducts.get(0);
            assertTrue(first.has("productName"), "热销商品应有productName");
            assertTrue(first.has("salesCount"), "热销商品应有salesCount");
            assertTrue(first.has("salesAmount"), "热销商品应有salesAmount");
        }
    }

    // ==================== TC-02: 空数据测试 ====================
    @Test
    @Order(2)
    @DisplayName("TC-02: 空数据 - 清空所有订单数据验证看板显示零值/空数组")
    public void testEmptyData() throws Exception {
        resetAllData();

        JsonNode summary = fetchDashboard("/api/admin/dashboard/summary");
        assertEquals("0", summary.path("todayRevenue").asText(), "营收应为0");
        assertEquals(0, summary.path("todayOrderCount").asInt(), "订单数应为0");
        assertEquals("0", summary.path("averageOrderValue").asText(), "平均客单价应为0");
        assertEquals(0, summary.path("inventoryWarningCount").asInt(), "预警数量应为0");

        JsonNode trend = fetchDashboard("/api/admin/dashboard/sales-trend");
        assertTrue(trend.isArray(), "销售趋势应为数组");
        assertEquals(7, trend.size(), "应返回7天数据");
        for (JsonNode day : trend) {
            assertEquals("0", day.path("revenue").asText(), "每天营收应为0");
            assertEquals(0, day.path("orderCount").asInt(), "每天订单数应为0");
        }

        JsonNode methods = fetchDashboard("/api/admin/dashboard/payment-methods");
        assertTrue(methods.isArray(), "支付方式应为数组");
        assertEquals(0, methods.size(), "无支付方式数据");

        JsonNode hotProducts = fetchDashboard("/api/admin/dashboard/hot-products");
        assertTrue(hotProducts.isArray(), "热销商品应为数组");
        assertEquals(0, hotProducts.size(), "无热销商品数据");
    }

    // ==================== TC-03: 跨天统计测试 ====================
    @Test
    @Order(3)
    @DisplayName("TC-03: 跨天统计 - 昨日PAID订单不应计入今日统计")
    public void testCrossDayStats() throws Exception {
        resetAllData();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime yesterdayTime = yesterday.atTime(10, 30, 0);

        Long orderId = createAndPayOrder(TABLE_ID, PRODUCT_1, 1, "WECHAT");

        jdbcTemplate.update(
            "UPDATE order_info SET paid_at = ?, created_at = ? WHERE id = ?",
            yesterdayTime.format(DATETIME_FMT),
            yesterdayTime.format(DATETIME_FMT),
            orderId
        );

        JsonNode summary = fetchDashboard("/api/admin/dashboard/summary");
        assertEquals("0", summary.path("todayRevenue").asText(), "昨日订单不计入今日营收");
        assertEquals(0, summary.path("todayOrderCount").asInt(), "昨日订单不计入今日订单数");

        JsonNode trend = fetchDashboard("/api/admin/dashboard/sales-trend");
        boolean foundYesterdayRevenue = false;
        boolean todayZero = true;
        LocalDate today = LocalDate.now();
        for (JsonNode day : trend) {
            String dateStr = day.path("date").asText();
            if (dateStr.equals(yesterday.format(DateTimeFormatter.ofPattern("MM-dd")))) {
                if (day.path("revenue").asDouble() > 0) foundYesterdayRevenue = true;
            }
            if (dateStr.equals(today.format(DateTimeFormatter.ofPattern("MM-dd")))) {
                if (day.path("revenue").asDouble() > 0) todayZero = false;
            }
        }
        assertTrue(foundYesterdayRevenue, "趋势图应包含昨日数据");
        assertTrue(todayZero, "今日数据应为0（因为只有昨日的订单）");
    }

    // ==================== TC-04: paid_at为NULL测试 ====================
    @Test
    @Order(4)
    @DisplayName("TC-04: paid_at为空 - 不计入营收统计")
    public void testPaidAtNull() throws Exception {
        resetAllData();

        Long orderId = createAndPayOrder(TABLE_ID, PRODUCT_1, 2, "WECHAT");

        // 强制将paid_at置为NULL，模拟支付时间缺失的异常数据
        jdbcTemplate.update("UPDATE order_info SET paid_at = NULL WHERE id = ?", orderId);

        JsonNode summary = fetchDashboard("/api/admin/dashboard/summary");

        BigDecimal revenue = new BigDecimal(summary.path("todayRevenue").asText());
        assertEquals(0, revenue.compareTo(BigDecimal.ZERO),
            "paid_at为NULL的订单不应计入今日营收");

        // 今日订单数按 created_at 统计全部订单，paid_at=NULL 不影响订单数统计
        assertEquals(1, summary.path("todayOrderCount").asInt(),
            "今日订单数按 created_at 统计，paid_at=NULL 的订单仍应计入订单数");

        JsonNode trend = fetchDashboard("/api/admin/dashboard/sales-trend");
        boolean foundInToday = false;
        for (JsonNode day : trend) {
            String dateStr = day.path("date").asText();
            if (dateStr.equals(LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd")))) {
                if (day.path("revenue").asDouble() > 0) foundInToday = true;
            }
        }
        assertFalse(foundInToday, "paid_at=NULL的订单不应出现在今日趋势中");
    }

    // ==================== TC-05: 库存预警测试 ====================
    @Test
    @Order(5)
    @DisplayName("TC-05: 库存预警 - stock < safe_stock时inventoryWarningCount递增")
    public void testInventoryWarning() throws Exception {
        resetAllData();

        JsonNode summaryBefore = fetchDashboard("/api/admin/dashboard/summary");
        int warningsBefore = summaryBefore.path("inventoryWarningCount").asInt();

        jdbcTemplate.update("UPDATE product SET stock = 5, safe_stock = 20 WHERE id = 1");

        JsonNode summaryAfter = fetchDashboard("/api/admin/dashboard/summary");
        int warningsAfter = summaryAfter.path("inventoryWarningCount").asInt();

        assertTrue(warningsAfter > warningsBefore,
            "设置stock<safe_stock后预警数量应增加");
        assertEquals(warningsBefore + 1, warningsAfter,
            "预警数量应恰好增加1");

        jdbcTemplate.update("UPDATE product SET stock = 100, safe_stock = 20 WHERE id = 1");

        JsonNode summaryReset = fetchDashboard("/api/admin/dashboard/summary");
        int warningsReset = summaryReset.path("inventoryWarningCount").asInt();
        assertEquals(warningsBefore, warningsReset,
            "恢复stock后预警数量应恢复原值");
    }

    // ==================== TC-06: 销售趋势补零测试 ====================
    @Test
    @Order(6)
    @DisplayName("TC-06: 销售趋势补零 - 无订单天显示revenue=0, orderCount=0")
    public void testSalesTrendZeroFill() throws Exception {
        resetAllData();

        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        Long orderId = createAndPayOrder(TABLE_ID, PRODUCT_1, 1, "WECHAT");

        jdbcTemplate.update(
            "UPDATE order_info SET paid_at = ?, created_at = ? WHERE id = ?",
            threeDaysAgo.atTime(14, 0, 0).format(DATETIME_FMT),
            threeDaysAgo.atTime(14, 0, 0).format(DATETIME_FMT),
            orderId
        );

        JsonNode trend = fetchDashboard("/api/admin/dashboard/sales-trend");
        assertEquals(7, trend.size(), "必须返回7天数据");

        int zeroDays = 0;
        int nonZeroDays = 0;
        Set<String> dates = new HashSet<>();
        for (JsonNode day : trend) {
            String dateStr = day.path("date").asText();
            dates.add(dateStr);
            double revenue = day.path("revenue").asDouble();
            int count = day.path("orderCount").asInt();

            if (revenue == 0 && count == 0) {
                zeroDays++;
            } else {
                nonZeroDays++;
            }
            assertTrue(day.has("date"), "每条记录应有date字段");
            assertTrue(day.has("revenue"), "每条记录应有revenue字段");
            assertTrue(day.has("orderCount"), "每条记录应有orderCount字段");
        }
        assertEquals(7, dates.size(), "7天日期不重复");
        assertTrue(zeroDays >= 5, "大部分天数应为零（仅1天有数据）");
        assertTrue(nonZeroDays >= 1, "至少1天有数据");
    }

    // ==================== TC-07: 支付方式占比测试 ====================
    @Test
    @Order(7)
    @DisplayName("TC-07: 支付方式占比 - 3种支付方式百分比之和=100%")
    public void testPaymentMethodRatio() throws Exception {
        resetAllData();

        Long order1 = createAndPayOrder(TABLE_ID, PRODUCT_1, 1, "WECHAT");   // 30元
        Long order2 = createAndPayOrder(TABLE_ID, PRODUCT_2, 2, "ALIPAY");   // 120元
        Long order3 = createAndPayOrder(TABLE_ID, PRODUCT_5, 6, "CASH");     // 150元 薯条
        // 总计: 300元

        assertNotNull(order1, "微信订单应创建成功, order1=" + order1);
        assertNotNull(order2, "支付宝订单应创建成功, order2=" + order2);
        assertNotNull(order3, "现金订单应创建成功, order3=" + order3);

        JsonNode methods = fetchDashboard("/api/admin/dashboard/payment-methods");
        assertEquals(3, methods.size(), "应有3种支付方式, 实际: " + methods.size());

        Map<String, Double> methodAmounts = new LinkedHashMap<>();
        BigDecimal totalFromApi = BigDecimal.ZERO;
        double totalPercent = 0;

        for (JsonNode m : methods) {
            String methodName = m.path("method").asText();
            double amount = m.path("amount").asDouble();
            double percent = m.path("percent").asDouble();
            methodAmounts.put(methodName, amount);
            totalFromApi = totalFromApi.add(BigDecimal.valueOf(amount));
            totalPercent += percent;
        }

        assertEquals(0, totalFromApi.compareTo(new BigDecimal("300")), "金额总和应为300");
        assertEquals(100.0, totalPercent, 0.1, "百分比之和应为100%");

        assertTrue(methodAmounts.containsKey("微信"), "应包含微信");
        assertTrue(methodAmounts.containsKey("支付宝"), "应包含支付宝");
        assertTrue(methodAmounts.containsKey("现金"), "应包含现金");

        double wechatPercent = methodAmounts.getOrDefault("微信", 0.0) / 300.0 * 100;
        double actualWechat = 0;
        for (JsonNode m : methods) {
            if ("微信".equals(m.path("method").asText())) {
                actualWechat = m.path("percent").asDouble();
            }
        }
        assertEquals(wechatPercent, actualWechat, 0.1, "微信占比应正确计算");
    }

    // ==================== TC-08: 热销商品排序测试 ====================
    @Test
    @Order(8)
    @DisplayName("TC-08: 热销商品排序 - 按销量降序且仅显示前5名")
    public void testHotProductSorting() throws Exception {
        resetAllData();

        createAndPayOrder(TABLE_ID, PRODUCT_5, 10, "WECHAT");   // 薯条x10 → 销量最高
        createAndPayOrder(TABLE_ID, PRODUCT_1, 5, "ALIPAY");    // 百威x5
        createAndPayOrder(TABLE_ID, PRODUCT_4, 3, "CASH");      // 青柠x3
        createAndPayOrder(TABLE_ID, PRODUCT_2, 2, "WECHAT");    // 长岛冰茶x2
        createAndPayOrder(TABLE_ID, PRODUCT_3, 1, "ALIPAY");    // 野格x1

        JsonNode hotProducts = fetchDashboard("/api/admin/dashboard/hot-products");
        assertTrue(hotProducts.isArray(), "热销商品应为数组");
        assertTrue(hotProducts.size() <= 5, "最多显示5个");
        assertEquals(5, hotProducts.size(), "应有5个热销商品");

        List<Integer> salesCounts = new ArrayList<>();
        List<String> productNames = new ArrayList<>();
        for (JsonNode p : hotProducts) {
            int count = p.path("salesCount").asInt();
            String name = p.path("productName").asText();
            salesCounts.add(count);
            productNames.add(name);
            assertTrue(p.has("productName"));
            assertTrue(p.has("salesCount"));
            assertTrue(p.has("salesAmount"));
            assertTrue(count > 0, "销量应大于0");
        }

        for (int i = 0; i < salesCounts.size() - 1; i++) {
            assertTrue(salesCounts.get(i) >= salesCounts.get(i + 1),
                "热销商品应按销量降序排列: index[" + i + "]=" + salesCounts.get(i) +
                " >= index[" + (i+1) + "]=" + salesCounts.get(i+1));
        }

        assertEquals("薯条", productNames.get(0), "薯条销量最高应排第一");
        assertEquals(10, salesCounts.get(0).intValue(), "薯条销量应为10");
    }

    // ==================== TC-09: 综合场景 - 完整业务流程验证 ====================
    @Test
    @Order(9)
    @DisplayName("TC-09: 综合场景 - 完整Dashboard数据一致性验证")
    public void testFullScenarioConsistency() throws Exception {
        resetAllData();

        createAndPayOrder(TABLE_ID, PRODUCT_1, 2, "WECHAT");   // 60元
        createAndPayOrder(TABLE_ID, PRODUCT_2, 1, "ALIPAY");   // 60元

        JsonNode summary = fetchDashboard("/api/admin/dashboard/summary");
        JsonNode trend = fetchDashboard("/api/admin/dashboard/sales-trend");
        JsonNode methods = fetchDashboard("/api/admin/dashboard/payment-methods");
        JsonNode hotProducts = fetchDashboard("/api/admin/dashboard/hot-products");

        BigDecimal summaryRevenue = new BigDecimal(summary.path("todayRevenue").asText());

        BigDecimal trendTotal = BigDecimal.ZERO;
        for (JsonNode day : trend) {
            trendTotal = trendTotal.add(new BigDecimal(day.path("revenue").asText()));
        }

        BigDecimal methodsTotal = BigDecimal.ZERO;
        for (JsonNode m : methods) {
            methodsTotal = methodsTotal.add(new BigDecimal(m.path("amount").asText()));
        }

        assertEquals(0, summaryRevenue.compareTo(trendTotal),
            "汇总营收应等于趋势图7天总和");
        assertEquals(0, summaryRevenue.compareTo(methodsTotal),
            "汇总营收应等于各支付方式金额总和");

        int summaryOrderCount = summary.path("todayOrderCount").asInt();
        int trendOrderCount = 0;
        for (JsonNode day : trend) {
            trendOrderCount += day.path("orderCount").asInt();
        }
        assertTrue(trendOrderCount >= summaryOrderCount,
            "趋势图订单总数应>=今日订单数（可能包含历史数据）");
    }
}
