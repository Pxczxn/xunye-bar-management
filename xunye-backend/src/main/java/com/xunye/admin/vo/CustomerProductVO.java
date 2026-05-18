package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerProductVO {

    private Long id;

    private String name;

    private Long categoryId;

    private String categoryName;

    private BigDecimal price;

    private String spec;

    private String unit;

    private Integer stock;

    private String description;

    private String imageUrl;

}
