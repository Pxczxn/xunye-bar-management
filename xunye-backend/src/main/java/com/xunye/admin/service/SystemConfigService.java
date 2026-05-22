package com.xunye.admin.service;

import com.xunye.admin.vo.MiniappConfigVO;
import com.xunye.admin.vo.OrderConfigVO;
import com.xunye.admin.vo.ShopConfigVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface SystemConfigService {

    /**
     * 获取店铺配置
     */
    ShopConfigVO getShopConfig();

    /**
     * 获取订单配置
     */
    OrderConfigVO getOrderConfig();

    /**
     * 获取小程序配置
     */
    MiniappConfigVO getMiniappConfig();

    /**
     * 批量更新配置
     */
    void updateConfigs(Map<String, String> configs);

    /**
     * 获取单个配置值
     */
    String getConfig(String key);

    /**
     * 上传小程序展示图片（轮播图）
     *
     * @return 图片相对URL
     */
    String uploadMiniappImage(MultipartFile file);

    /**
     * 删除小程序展示图片
     */
    void deleteMiniappImage(String imageUrl);

}
