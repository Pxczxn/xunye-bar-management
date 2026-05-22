package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.entity.SystemConfig;
import com.xunye.admin.mapper.SystemConfigMapper;
import com.xunye.admin.service.SystemConfigService;
import com.xunye.admin.vo.MiniappConfigVO;
import com.xunye.admin.vo.OrderConfigVO;
import com.xunye.admin.vo.ShopConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper configMapper;
    private final ObjectMapper objectMapper;

    @Value("${file.upload.base-path}")
    private String fileUploadBasePath;

    @Value("${file.upload.miniapp-path:/miniapp}")
    private String miniappPath;

    @Override
    public ShopConfigVO getShopConfig() {
        Map<String, String> configMap = getConfigMapByPrefix("shop.");

        ShopConfigVO vo = new ShopConfigVO();
        vo.setName(configMap.getOrDefault("shop.name", "寻野"));
        vo.setSlogan(configMap.getOrDefault("shop.slogan", "乘兴而去，尽兴而归。"));
        vo.setAddress(configMap.getOrDefault("shop.address", ""));
        vo.setPhone(configMap.getOrDefault("shop.phone", ""));
        vo.setContactWechat(configMap.getOrDefault("shop.contact_wechat", ""));
        vo.setBusinessHours(configMap.getOrDefault("shop.business_hours", "18:00 - 02:00"));
        vo.setNotice(configMap.getOrDefault("shop.notice", "未成年人禁止饮酒，请理性消费。"));
        return vo;
    }

    @Override
    public OrderConfigVO getOrderConfig() {
        Map<String, String> configMap = getConfigMapByPrefix("order.");

        OrderConfigVO vo = new OrderConfigVO();

        // 支付方式
        String paymentMethodsStr = configMap.get("order.payment_methods");
        if (paymentMethodsStr != null) {
            try {
                vo.setPaymentMethods(objectMapper.readValue(paymentMethodsStr, new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("解析支付方式配置失败: {}", paymentMethodsStr, e);
                vo.setPaymentMethods(List.of("WECHAT", "ALIPAY", "CASH"));
            }
        } else {
            vo.setPaymentMethods(List.of("WECHAT", "ALIPAY", "CASH"));
        }

        // 吧台打印
        vo.setReceiptBar(parseReceiptConfig(configMap.get("order.receipt_bar")));

        // 后厨打印
        vo.setReceiptKitchen(parseReceiptConfig(configMap.get("order.receipt_kitchen")));

        // 取消超时
        vo.setCancelTimeout(configMap.getOrDefault("order.cancel_timeout", "30"));

        return vo;
    }

    @Override
    public MiniappConfigVO getMiniappConfig() {
        Map<String, String> configMap = getConfigMapByPrefix("miniapp.");

        MiniappConfigVO vo = new MiniappConfigVO();
        vo.setHomepageTitle(getConfigValue(configMap, "miniapp.homepage_title", "寻野酒吧"));
        vo.setHomepageSubtitle(getConfigValue(configMap, "miniapp.homepage_subtitle", "乘兴而去，尽兴而归。"));
        vo.setMenuDisplay(getConfigValue(configMap, "miniapp.menu_display", "all"));

        String scanToOrder = getConfigValue(configMap, "miniapp.scan_to_order", "true");
        vo.setScanToOrder(!"false".equalsIgnoreCase(scanToOrder));

        // 解析轮播图列表
        String bannerImagesStr = configMap.get("miniapp.banner_images");
        if (bannerImagesStr != null) {
            try {
                vo.setBannerImages(objectMapper.readValue(bannerImagesStr, new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("解析轮播图配置失败: {}", bannerImagesStr, e);
                vo.setBannerImages(new ArrayList<>());
            }
        } else {
            vo.setBannerImages(new ArrayList<>());
        }

        return vo;
    }

    @Override
    public String uploadMiniappImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path uploadPath = Paths.get(fileUploadBasePath, miniappPath);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            return "/images" + miniappPath + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteMiniappImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        // 1. 删除物理文件
        try {
            // 从URL中提取相对路径，如 /images/miniapp/xxx.jpg
            String relativePath = imageUrl.replace("/images", "");
            Path filePath = Paths.get(fileUploadBasePath, relativePath);
            Files.deleteIfExists(filePath);
            log.info("已删除图片文件: {}", filePath);
        } catch (IOException e) {
            log.warn("删除图片文件失败: {}", imageUrl, e);
        }

        // 2. 从配置中移除该图片URL
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, "miniapp.banner_images");
        SystemConfig config = configMapper.selectOne(wrapper);

        if (config != null && config.getConfigValue() != null) {
            try {
                List<String> images = objectMapper.readValue(config.getConfigValue(), new TypeReference<List<String>>() {});
                images.remove(imageUrl);
                config.setConfigValue(objectMapper.writeValueAsString(images));
                configMapper.updateById(config);
            } catch (JsonProcessingException e) {
                log.warn("更新轮播图配置失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void updateConfigs(Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : configs.entrySet()) {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, entry.getKey());
            SystemConfig existing = configMapper.selectOne(wrapper);

            if (existing != null) {
                existing.setConfigValue(entry.getValue());
                configMapper.updateById(existing);
            } else {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                config.setDescription(entry.getKey());
                configMapper.insert(config);
            }
        }
    }

    @Override
    public String getConfig(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = configMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    private String getConfigValue(Map<String, String> configMap, String key, String defaultValue) {
        String value = configMap.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        value = value.trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private Map<String, String> getConfigMapByPrefix(String prefix) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SystemConfig::getConfigKey, prefix);
        List<SystemConfig> configs = configMapper.selectList(wrapper);
        return configs.stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue));
    }

    private OrderConfigVO.ReceiptConfig parseReceiptConfig(String json) {
        if (json != null) {
            try {
                return objectMapper.readValue(json, OrderConfigVO.ReceiptConfig.class);
            } catch (JsonProcessingException e) {
                log.warn("解析打印配置失败: {}", json, e);
            }
        }
        OrderConfigVO.ReceiptConfig config = new OrderConfigVO.ReceiptConfig();
        config.setEnabled(false);
        config.setPrinter("USB");
        return config;
    }

}
