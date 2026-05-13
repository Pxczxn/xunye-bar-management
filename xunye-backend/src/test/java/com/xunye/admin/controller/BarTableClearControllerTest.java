package com.xunye.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.dto.OrderItemDTO;
import com.xunye.admin.dto.OrderPayDTO;
import com.xunye.admin.entity.BarTable;
import com.xunye.admin.mapper.BarTableMapper;
import com.xunye.admin.mapper.OrderInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BarTableClearControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private BarTableMapper barTableMapper;
    @Autowired private OrderInfoMapper orderInfoMapper;

    private static final Long TABLE_ID = 1L;
    private static final Long PRODUCT_ID = 1L;
    private static String token;

    @BeforeEach
    void resetData() {
        jdbcTemplate.update("DELETE FROM order_item");
        jdbcTemplate.update("DELETE FROM order_info");
        jdbcTemplate.update("UPDATE product SET stock = 86 WHERE id = ?", PRODUCT_ID);
        jdbcTemplate.update("UPDATE bar_table SET status = 'EMPTY' WHERE id = ?", TABLE_ID);
    }

    @Test
    void clearTableShouldRejectActiveOrder() throws Exception {
        createPaidOrder();

        mockMvc.perform(patch("/api/admin/tables/{id}/clear", TABLE_ID)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(jsonPath("$.code").value(500));

        assertEquals("USING", barTableMapper.selectById(TABLE_ID).getStatus());
    }

    @Test
    void clearTableShouldSucceedAfterServeFinished() throws Exception {
        Long orderId = createPaidOrder();

        mockMvc.perform(patch("/api/admin/orders/{id}/making", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/orders/{id}/finish", orderId)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/tables/{id}/clear", TABLE_ID)
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        BarTable table = barTableMapper.selectById(TABLE_ID);
        assertEquals("EMPTY", table.getStatus());
        assertEquals("FINISHED", orderInfoMapper.selectById(orderId).getServeStatus());
    }

    private Long createPaidOrder() throws Exception {
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(PRODUCT_ID);
        item.setQuantity(1);

        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setTableId(TABLE_ID);
        createDTO.setItems(List.of(item));

        MvcResult result = mockMvc.perform(post("/api/admin/orders")
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn();
        Long orderId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").asLong();

        OrderPayDTO payDTO = new OrderPayDTO();
        payDTO.setPaymentMethod("WECHAT");
        mockMvc.perform(patch("/api/admin/orders/{id}/pay", orderId)
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payDTO)))
                .andExpect(status().isOk());

        return orderId;
    }

    private String getToken() throws Exception {
        if (token == null) {
            MvcResult r = mockMvc.perform(post("/api/admin/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "username", "admin",
                            "password", "123456"
                    ))))
                    .andExpect(status().isOk())
                    .andReturn();
            token = objectMapper.readTree(r.getResponse().getContentAsString())
                    .path("data").path("token").asText();
        }
        return token;
    }
}
