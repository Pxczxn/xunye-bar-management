package com.xunye.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createPaymentThenConfirmShouldPayOrder() throws Exception {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS payment_order (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "payment_no VARCHAR(64) NOT NULL UNIQUE, " +
                "order_id BIGINT NOT NULL, " +
                "order_no VARCHAR(64) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "provider VARCHAR(20) NOT NULL, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                "transaction_id VARCHAR(128) DEFAULT NULL, " +
                "created_at DATETIME NOT NULL, " +
                "paid_at DATETIME DEFAULT NULL, " +
                "KEY idx_order_id (order_id), " +
                "KEY idx_order_no (order_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        String orderNo = "PAY_TEST_" + System.currentTimeMillis();
        Long tableId = jdbcTemplate.queryForObject("SELECT id FROM bar_table LIMIT 1", Long.class);
        jdbcTemplate.update("""
                INSERT INTO order_info
                (order_no, table_id, table_name, total_amount, status, serve_status, source, created_at, deleted)
                VALUES (?, ?, 'A1', ?, 'UNPAID', 'PENDING', 'CUSTOMER_MINI', NOW(), 0)
                """, orderNo, tableId, new BigDecimal("12.00"));

        JsonNode created = postJson("/api/customer/orders/" + orderNo + "/payments");
        assertEquals(200, created.path("code").asInt(), created.toString());
        String paymentNo = created.path("data").path("paymentNo").asText();
        assertFalse(paymentNo.isBlank());
        assertEquals("PENDING", created.path("data").path("status").asText());

        JsonNode queriedBeforeConfirm = getJson("/api/customer/payments/" + paymentNo);
        assertEquals(200, queriedBeforeConfirm.path("code").asInt(), queriedBeforeConfirm.toString());
        assertEquals(orderNo, queriedBeforeConfirm.path("data").path("orderNo").asText());
        assertEquals("PENDING", queriedBeforeConfirm.path("data").path("status").asText());

        JsonNode confirmed = postJson("/api/customer/payments/" + paymentNo + "/confirm");
        assertEquals(200, confirmed.path("code").asInt(), confirmed.toString());

        JsonNode queriedAfterConfirm = getJson("/api/customer/payments/" + paymentNo);
        assertEquals(200, queriedAfterConfirm.path("code").asInt(), queriedAfterConfirm.toString());
        assertEquals("SUCCESS", queriedAfterConfirm.path("data").path("status").asText());

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM order_info WHERE order_no = ?", String.class, orderNo);
        String paymentMethod = jdbcTemplate.queryForObject(
                "SELECT payment_method FROM order_info WHERE order_no = ?", String.class, orderNo);
        Integer paidAtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_info WHERE order_no = ? AND paid_at IS NOT NULL", Integer.class, orderNo);

        assertEquals("PAID", status);
        assertEquals("WECHAT", paymentMethod);
        assertTrue(paidAtCount != null && paidAtCount == 1);
    }

    private JsonNode postJson(String path) throws Exception {
        MvcResult result = mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getJson(String path) throws Exception {
        MvcResult result = mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
