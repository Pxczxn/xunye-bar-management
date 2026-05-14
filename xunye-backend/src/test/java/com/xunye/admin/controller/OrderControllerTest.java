package com.xunye.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.dto.OrderItemDTO;
import com.xunye.admin.dto.OrderPayDTO;
import com.xunye.admin.entity.BarTable;
import com.xunye.admin.entity.OrderInfo;
import com.xunye.admin.entity.Product;
import com.xunye.admin.mapper.BarTableMapper;
import com.xunye.admin.mapper.OrderInfoMapper;
import com.xunye.admin.mapper.ProductMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private BarTableMapper barTableMapper;
    @Autowired private OrderInfoMapper orderInfoMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final Long PRODUCT_ID = 1L;
    private static final Long TABLE_ID_1 = 1L;
    private static final Long TABLE_ID_2 = 2L;
    private static final int PRODUCT_INITIAL_STOCK = 86;
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

    private void resetTestData() {
        jdbcTemplate.update("DELETE FROM order_item");
        jdbcTemplate.update("DELETE FROM order_info");
        jdbcTemplate.update("UPDATE product SET stock = ? WHERE id = ?", PRODUCT_INITIAL_STOCK, PRODUCT_ID);
        jdbcTemplate.update("UPDATE bar_table SET status = 'EMPTY' WHERE id IN (?, ?)", TABLE_ID_1, TABLE_ID_2);
    }

    private Long createTestOrder(Long tableId, int quantity) throws Exception {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setTableId(tableId);
        createDTO.setRemark("测试订单");
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(PRODUCT_ID);
        itemDTO.setQuantity(quantity);
        createDTO.setItems(Arrays.asList(itemDTO));

        MvcResult result = mockMvc.perform(post("/api/admin/orders")
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").asLong();
    }

    @Test @Order(1)
    @DisplayName("TC-01: 创建订单 - 验证库存扣减和桌台状态变更")
    public void testCreateOrder() throws Exception {
        resetTestData();
        int stockBefore = productMapper.selectById(PRODUCT_ID).getStock();
        assertEquals(PRODUCT_INITIAL_STOCK, stockBefore, "初始库存应为86");
        assertEquals("EMPTY", barTableMapper.selectById(TABLE_ID_1).getStatus(), "桌台初始状态应为EMPTY");

        Long orderId = createTestOrder(TABLE_ID_1, 2);
        assertTrue(orderId > 0, "订单ID应该大于0");
        assertEquals(stockBefore - 2, productMapper.selectById(PRODUCT_ID).getStock(), "库存应该减少2个单位");
        assertEquals("USING", barTableMapper.selectById(TABLE_ID_1).getStatus(), "桌台状态应该变更为USING");
        OrderInfo order = orderInfoMapper.selectById(orderId);
        assertEquals("UNPAID", order.getStatus(), "新创建的订单状态应为UNPAID");
        assertNotNull(order.getOrderNo(), "订单编号不应为空");
        assertEquals("A1", order.getTableName(), "桌台名称应为A1");
    }

    @Test @Order(2)
    @DisplayName("TC-02: 订单支付 - 验证订单状态变更为PAID")
    public void testPayOrder() throws Exception {
        resetTestData();
        Long orderId = createTestOrder(TABLE_ID_1, 2);
        assertEquals("UNPAID", orderInfoMapper.selectById(orderId).getStatus(), "支付前订单状态应为UNPAID");

        OrderPayDTO payDTO = new OrderPayDTO();
        payDTO.setPaymentMethod("WECHAT");
        mockMvc.perform(patch("/api/admin/orders/{id}/pay", orderId)
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payDTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

        OrderInfo paidOrder = orderInfoMapper.selectById(orderId);
        assertEquals("PAID", paidOrder.getStatus(), "订单状态应该变更为PAID");
        assertEquals("WECHAT", paidOrder.getPaymentMethod(), "支付方式应该为WECHAT");
        assertNotNull(paidOrder.getPaidAt(), "支付时间不应该为空");
    }

    @Test @Order(3)
    @DisplayName("TC-03: 取消订单 - 验证库存回滚和桌台状态恢复")
    public void testCancelOrder() throws Exception {
        resetTestData();
        Long orderId = createTestOrder(TABLE_ID_2, 3);
        assertEquals(PRODUCT_INITIAL_STOCK - 3, productMapper.selectById(PRODUCT_ID).getStock(), "创建订单后库存应该减少3个单位");
        assertEquals("USING", barTableMapper.selectById(TABLE_ID_2).getStatus(), "A2桌台状态应该变为USING");

        mockMvc.perform(patch("/api/admin/orders/{id}/cancel", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

        assertEquals(PRODUCT_INITIAL_STOCK, productMapper.selectById(PRODUCT_ID).getStock(), "取消订单后库存应该恢复");
        OrderInfo cancelledOrder = orderInfoMapper.selectById(orderId);
        assertEquals("CANCELLED", cancelledOrder.getStatus(), "订单状态应该变更为CANCELLED");
        assertNotNull(cancelledOrder.getCancelledAt(), "取消时间不应该为空");
        assertEquals("EMPTY", barTableMapper.selectById(TABLE_ID_2).getStatus(), "A2桌台状态应该恢复为EMPTY");
    }

    @Test @Order(4)
    @DisplayName("TC-04: 获取最近订单接口 - 验证返回格式和数据")
    public void testGetRecentOrders() throws Exception {
        resetTestData();
        createTestOrder(TABLE_ID_1, 1);
        createTestOrder(TABLE_ID_2, 2);

        MvcResult result = mockMvc.perform(get("/api/admin/orders/recent")
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray()).andReturn();

        int dataLength = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").size();
        assertTrue(dataLength <= 8, "返回数据条数不应超过8");
        assertTrue(dataLength > 0, "应至少返回1条订单数据");
    }

    @Test @Order(5)
    @DisplayName("TC-05: 分页查询订单接口 - 验证分页格式")
    public void testGetOrderPage() throws Exception {
        resetTestData();
        createTestOrder(TABLE_ID_1, 1);

        mockMvc.perform(get("/api/admin/orders").header("Authorization", "Bearer " + getToken())
                .param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test @Order(6)
    @DisplayName("TC-06: Order page filters source, serveStatus and excludeStatus together")
    public void testGetOrderPageWithSourceServeStatusAndExcludeStatus() throws Exception {
        resetTestData();
        Long paidPendingOrderId = createTestOrder(TABLE_ID_1, 1);

        OrderPayDTO payDTO = new OrderPayDTO();
        payDTO.setPaymentMethod("WECHAT");
        mockMvc.perform(patch("/api/admin/orders/{id}/pay", paidPendingOrderId)
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payDTO)))
                .andExpect(status().isOk());

        Long cancelledOrderId = createTestOrder(TABLE_ID_2, 1);
        mockMvc.perform(patch("/api/admin/orders/{id}/cancel", cancelledOrderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/orders")
                .header("Authorization", "Bearer " + getToken())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("source", "ADMIN_POS")
                .param("serveStatus", "PENDING")
                .param("excludeStatus", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(paidPendingOrderId))
                .andExpect(jsonPath("$.data.records[0].source").value("ADMIN_POS"))
                .andExpect(jsonPath("$.data.records[0].serveStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.records[0].status").value("PAID"));
    }

    @Test @Order(7)
    @DisplayName("TC-06: 查询订单详情接口 - 验证详情格式")
    public void testGetOrderDetail() throws Exception {
        resetTestData();
        Long orderId = createTestOrder(TABLE_ID_1, 2);

        mockMvc.perform(get("/api/admin/orders/{id}", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.orderNo").isString())
                .andExpect(jsonPath("$.data.tableId").isNumber())
                .andExpect(jsonPath("$.data.tableName").isString())
                .andExpect(jsonPath("$.data.totalAmount").isNumber())
                .andExpect(jsonPath("$.data.status").isString())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].productId").isNumber())
                .andExpect(jsonPath("$.data.items[0].productName").isString())
                .andExpect(jsonPath("$.data.items[0].quantity").isNumber())
                .andExpect(jsonPath("$.data.items[0].price").isNumber())
                .andExpect(jsonPath("$.data.items[0].amount").isNumber());
    }

    @Test @Order(7)
    @DisplayName("TC-07: 支付已支付订单应失败")
    public void testPayAlreadyPaidOrder() throws Exception {
        resetTestData();
        Long orderId = createTestOrder(TABLE_ID_1, 1);
        OrderPayDTO payDTO = new OrderPayDTO();
        payDTO.setPaymentMethod("WECHAT");

        mockMvc.perform(patch("/api/admin/orders/{id}/pay", orderId)
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/orders/{id}/pay", orderId)
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payDTO)))
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test @Order(8)
    @DisplayName("TC-08: 取消已支付订单应失败")
    public void testCancelPaidOrder() throws Exception {
        resetTestData();
        Long orderId = createTestOrder(TABLE_ID_1, 1);
        OrderPayDTO payDTO = new OrderPayDTO();
        payDTO.setPaymentMethod("CASH");

        mockMvc.perform(patch("/api/admin/orders/{id}/pay", orderId)
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/orders/{id}/cancel", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(jsonPath("$.code").value(500));

        assertEquals("PAID", orderInfoMapper.selectById(orderId).getStatus(), "订单状态不应改变");
    }

    @Test @Order(9)
    @DisplayName("TC-09: 未支付订单不能开始制作")
    public void testUnpaidOrderCannotStartMaking() throws Exception {
        resetTestData();
        Long orderId = createTestOrder(TABLE_ID_1, 1);

        mockMvc.perform(patch("/api/admin/orders/{id}/making", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(jsonPath("$.code").value(500));

        OrderInfo order = orderInfoMapper.selectById(orderId);
        assertEquals("UNPAID", order.getStatus(), "订单仍应保持未支付");
        assertEquals("PENDING", order.getServeStatus(), "未支付订单不能进入制作中");
    }

    @Test @Order(10)
    @DisplayName("TC-10: 制作完成只更新制作状态，不自动清台")
    public void testFinishServeDoesNotReleaseTable() throws Exception {
        resetTestData();
        Long orderId = createTestOrder(TABLE_ID_1, 1);
        OrderPayDTO payDTO = new OrderPayDTO();
        payDTO.setPaymentMethod("WECHAT");

        mockMvc.perform(patch("/api/admin/orders/{id}/pay", orderId)
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/orders/{id}/making", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(patch("/api/admin/orders/{id}/finish", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        OrderInfo order = orderInfoMapper.selectById(orderId);
        assertEquals("PAID", order.getStatus(), "制作完成不应改变支付状态");
        assertEquals("FINISHED", order.getServeStatus(), "制作状态应变为已完成");
        assertEquals("USING", barTableMapper.selectById(TABLE_ID_1).getStatus(), "制作完成不等于清台，桌台仍应占用");
    }

    @Test @Order(11)
    @DisplayName("TC-09: 库存不足时创建订单应失败")
    public void testCreateOrderWithInsufficientStock() throws Exception {
        resetTestData();
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setTableId(TABLE_ID_1);
        createDTO.setRemark("库存不足测试");
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(PRODUCT_ID);
        itemDTO.setQuantity(9999);
        createDTO.setItems(Arrays.asList(itemDTO));

        mockMvc.perform(post("/api/admin/orders")
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(jsonPath("$.code").value(500));

        assertEquals(PRODUCT_INITIAL_STOCK, productMapper.selectById(PRODUCT_ID).getStock(), "库存不应被扣减");
        assertEquals("EMPTY", barTableMapper.selectById(TABLE_ID_1).getStatus(), "桌台状态不应改变");
    }
}
