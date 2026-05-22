package com.xunye.admin.vo;

import lombok.Data;

import java.util.List;

@Data
public class MiniappConfigVO {

    private String homepageTitle;

    private String homepageSubtitle;

    private String menuDisplay;

    private Boolean scanToOrder;

    /**
     * 首页轮播图（图片URL列表）
     */
    private List<String> bannerImages;

}
