package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.LoginDTO;
import com.xunye.admin.entity.StaffUser;
import com.xunye.admin.mapper.StaffUserMapper;
import com.xunye.admin.service.AuthService;
import com.xunye.admin.vo.LoginVO;
import com.xunye.admin.vo.ProfileVO;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private final StaffUserMapper staffUserMapper;
    private final PasswordEncoder passwordEncoder;

    private static class TokenInfo {
        final Long userId;
        final String role;
        final long expireAt;

        TokenInfo(Long userId, String role, long expireAt) {
            this.userId = userId;
            this.role = role;
            this.expireAt = expireAt;
        }
    }

    private static final ConcurrentHashMap<String, TokenInfo> TOKEN_STORE = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRE_MS = 8 * 60 * 60 * 1000L;

    private static final ScheduledExecutorService TOKEN_CLEANER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "token-cleaner");
                t.setDaemon(true);
                return t;
            });

    public AuthServiceImpl(StaffUserMapper staffUserMapper, PasswordEncoder passwordEncoder) {
        this.staffUserMapper = staffUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        TOKEN_CLEANER.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            TOKEN_STORE.entrySet().removeIf(entry -> entry.getValue().expireAt < now);
        }, 10, 10, TimeUnit.MINUTES);
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

        String storedPassword = staffUser.getPassword();
        boolean passwordMatch;

        if (isBCryptHash(storedPassword)) {
            passwordMatch = passwordEncoder.matches(loginDTO.getPassword(), storedPassword);
        } else {
            passwordMatch = storedPassword.equals(loginDTO.getPassword());
            if (passwordMatch) {
                staffUser.setPassword(passwordEncoder.encode(loginDTO.getPassword()));
                staffUserMapper.updateById(staffUser);
            }
        }

        if (!passwordMatch) {
            throw new BusinessException(400, "账号或密码错误");
        }

        staffUser.setLastLoginAt(LocalDateTime.now());
        staffUserMapper.updateById(staffUser);

        String token = "admin-token-" + UUID.randomUUID().toString().replace("-", "");
        long expireAt = System.currentTimeMillis() + TOKEN_EXPIRE_MS;
        TOKEN_STORE.put(token, new TokenInfo(staffUser.getId(), staffUser.getRole(), expireAt));

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

        TokenInfo tokenInfo = TOKEN_STORE.get(token);
        StaffUser staffUser = staffUserMapper.selectById(tokenInfo.userId);
        if (staffUser == null || staffUser.getStatus() == null || staffUser.getStatus() != 1) {
            TOKEN_STORE.remove(token);
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
        TokenInfo info = TOKEN_STORE.get(token);
        if (info == null) {
            return "未登录，请先登录";
        }
        if (info.expireAt < System.currentTimeMillis()) {
            TOKEN_STORE.remove(token);
            return "登录已过期，请重新登录";
        }
        return null;
    }

    @Override
    public Long getUserIdByToken(String token) {
        TokenInfo info = TOKEN_STORE.get(token);
        if (info == null || info.expireAt < System.currentTimeMillis()) {
            return null;
        }
        return info.userId;
    }

    @Override
    public String getRoleByToken(String token) {
        TokenInfo info = TOKEN_STORE.get(token);
        if (info == null || info.expireAt < System.currentTimeMillis()) {
            return null;
        }
        return info.role;
    }

    private boolean isBCryptHash(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$"));
    }
}
