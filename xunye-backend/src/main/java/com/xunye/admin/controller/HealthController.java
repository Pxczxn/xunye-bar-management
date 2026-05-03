package com.xunye.admin.controller;

import com.xunye.admin.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口
 * 用于检测服务是否正常运行
 */
@RestController
@RequestMapping("/api/admin")
public class HealthController {

    /**
     * 健康检查
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("xunye admin api running");
    }

}
