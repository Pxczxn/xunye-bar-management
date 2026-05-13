package com.xunye.admin.service;

import com.xunye.admin.dto.LoginDTO;
import com.xunye.admin.vo.LoginVO;
import com.xunye.admin.vo.ProfileVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 员工登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     */
    ProfileVO getProfile(String token);

    /**
     * 验证token有效性，返回null表示有效，返回错误信息表示无效
     */
    String validateToken(String token);

    /**
     * 根据token获取用户ID，无效返回null
     */
    Long getUserIdByToken(String token);

    /**
     * 根据token获取用户角色，无效返回null
     */
    String getRoleByToken(String token);

}
