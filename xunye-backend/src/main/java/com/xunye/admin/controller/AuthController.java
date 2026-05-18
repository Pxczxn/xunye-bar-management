package com.xunye.admin.controller;

import com.xunye.admin.annotation.RateLimited;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.LoginDTO;
import com.xunye.admin.service.AuthService;
import com.xunye.admin.vo.LoginVO;
import com.xunye.admin.vo.ProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @RateLimited(limit = 5, period = 60, key = "admin_login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResponse.success(authService.login(dto));
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileVO> getProfile(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        return ApiResponse.success(authService.getProfile(token));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        if (token != null) {
            authService.logout(token);
        }
        return ApiResponse.success(null);
    }

}
