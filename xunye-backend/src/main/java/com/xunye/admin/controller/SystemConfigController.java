package com.xunye.admin.controller;

import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.SystemConfigUpdateDTO;
import com.xunye.admin.service.SystemConfigService;
import com.xunye.admin.vo.MiniappConfigVO;
import com.xunye.admin.vo.OrderConfigVO;
import com.xunye.admin.vo.ShopConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@RequireRole({"BOSS"})
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取店铺配置
     */
    @GetMapping("/shop")
    public ApiResponse<ShopConfigVO> getShopConfig() {
        return ApiResponse.success(systemConfigService.getShopConfig());
    }

    /**
     * 获取订单配置
     */
    @GetMapping("/order")
    public ApiResponse<OrderConfigVO> getOrderConfig() {
        return ApiResponse.success(systemConfigService.getOrderConfig());
    }

    /**
     * 获取小程序配置
     */
    @GetMapping("/miniapp")
    public ApiResponse<MiniappConfigVO> getMiniappConfig() {
        return ApiResponse.success(systemConfigService.getMiniappConfig());
    }

    /**
     * 批量更新配置
     */
    @PutMapping
    public ApiResponse<Void> updateConfigs(@RequestBody SystemConfigUpdateDTO dto) {
        systemConfigService.updateConfigs(dto.getConfigs());
        return ApiResponse.success();
    }

    /**
     * 获取单条配置
     */
    @GetMapping("/{key}")
    public ApiResponse<Map<String, String>> getConfig(@PathVariable String key) {
        String value = systemConfigService.getConfig(key);
        return ApiResponse.success(Map.of(key, value != null ? value : ""));
    }

    /**
     * 上传小程序展示图片（酒水小吃轮播图）
     */
    @PostMapping("/miniapp/upload-image")
    public ApiResponse<String> uploadMiniappImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = systemConfigService.uploadMiniappImage(file);
            return ApiResponse.success(imageUrl);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            log.error("上传小程序图片失败", e);
            return ApiResponse.error("文件上传失败");
        }
    }

    /**
     * 删除小程序展示图片
     */
    @DeleteMapping("/miniapp/images")
    public ApiResponse<Void> deleteMiniappImage(@RequestParam String imageUrl) {
        systemConfigService.deleteMiniappImage(imageUrl);
        return ApiResponse.success();
    }

}
