package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.LoginDTO;
import com.xunye.admin.entity.StaffUser;
import com.xunye.admin.mapper.StaffUserMapper;
import com.xunye.admin.service.AuthService;
import com.xunye.admin.vo.LoginVO;
import com.xunye.admin.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StaffUserMapper staffUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_HOURS = 8;

    static class TokenInfo {
        public Long userId;
        public String role;

        public TokenInfo() {}

        public TokenInfo(Long userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<StaffUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffUser::getUsername, loginDTO.getUsername());
        wrapper.last("LIMIT 1");
        StaffUser staffUser = staffUserMapper.selectOne(wrapper);

        if (staffUser == null) {
            throw new BusinessException(400, "账号或密码错误");
        }

        if (staffUser.getStatus() == null || staffUser.getStatus() != 1) {
            throw new BusinessException(400, "该账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), staffUser.getPassword())) {
            throw new BusinessException(400, "账号或密码错误");
        }

        staffUser.setLastLoginAt(LocalDateTime.now());
        staffUserMapper.updateById(staffUser);

        String token = "admin-token-" + UUID.randomUUID().toString().replace("-", "");
        TokenInfo tokenInfo = new TokenInfo(staffUser.getId(), staffUser.getRole());
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, tokenInfo, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);

        LoginVO.LoginUserVO loginUserVO = new LoginVO.LoginUserVO();
        loginUserVO.setId(staffUser.getId());
        loginUserVO.setUsername(staffUser.getUsername());
        loginUserVO.setNickname(staffUser.getNickname());
        loginUserVO.setRole(staffUser.getRole());
        loginVO.setUser(loginUserVO);

        return loginVO;
    }

    @Override
    public ProfileVO getProfile(String token) {
        String errorMsg = validateToken(token);
        if (errorMsg != null) {
            throw new BusinessException(401, errorMsg);
        }

        TokenInfo tokenInfo = (TokenInfo) redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (tokenInfo == null) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        StaffUser staffUser = staffUserMapper.selectById(tokenInfo.userId);
        if (staffUser == null || staffUser.getStatus() == null || staffUser.getStatus() != 1) {
            redisTemplate.delete(TOKEN_PREFIX + token);
            throw new BusinessException(401, "用户不存在或已被禁用");
        }

        ProfileVO profileVO = new ProfileVO();
        profileVO.setId(staffUser.getId());
        profileVO.setUsername(staffUser.getUsername());
        profileVO.setNickname(staffUser.getNickname());
        profileVO.setRole(staffUser.getRole());
        return profileVO;
    }

    @Override
    public String validateToken(String token) {
        TokenInfo info = (TokenInfo) redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (info == null) {
            return "未登录，请先登录";
        }
        return null;
    }

    @Override
    public Long getUserIdByToken(String token) {
        TokenInfo info = (TokenInfo) redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (info == null) {
            return null;
        }
        return info.userId;
    }

    @Override
    public String getRoleByToken(String token) {
        TokenInfo info = (TokenInfo) redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (info == null) {
            return null;
        }
        return info.role;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }
}
