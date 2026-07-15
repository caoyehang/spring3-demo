package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.Vo.ApiRest;
import org.example.entity.User;
import org.example.enums.ApiErrorEnum;
import org.example.mapper.UserMapper;
import org.example.service.IUserService;
import org.example.utils.JwtUtils;
import org.example.utils.RedisUtils;
import org.example.utils.RsaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.PrivateKey;

/**
 * 用户服务实现。
 * <p>
 * 主要负责登录校验、JWT 生成和用户分页查询。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final UserMapper userMapper;
    private final RedisUtils redisUtils;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, RedisUtils redisUtils) {
        this.userMapper = userMapper;
        this.redisUtils = redisUtils;
    }

    /**
     * 登录流程：校验入参 -> 校验账号密码 -> 校验验证码 -> 生成 JWT。
     */
    @Override
    public ApiRest<String> login(String username, String password, String uuid, String code) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_USERNAME_NULL);
        }
        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(code)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_CODE_UUID_NULL);
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null || !password.equals(user.getPassword())) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_USERNAME_PASSWORD_INVALID);
        }

        String verifyCode = redisUtils.get(uuid);
        if (!StringUtils.hasText(verifyCode)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_UUID_INVALID);
        }
        if (!verifyCode.equalsIgnoreCase(code)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_CODE_INVALID);
        }

        try {
            PrivateKey privateKey = loadPrivateKey();
            String token = JwtUtils.generateTokenExpireInMinutes(user, privateKey, 120);
            return ApiRest.success(token);
        } catch (Exception e) {
            return ApiRest.failure(ApiErrorEnum.SYSTEM_ERROR, e.getMessage());
        }
    }

    @Override
    public ApiRest<Void> logout() {
        return ApiRest.success();
    }

    /**
     * 从 classpath 读取 RSA 私钥，用于签发 JWT。
     */
    private PrivateKey loadPrivateKey() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("key/pri_rsa");
        return RsaUtils.getPrivateKey(classPathResource.getContentAsByteArray());
    }

    /**
     * 用户分页查询，具体 SQL 在 UserMapper.xml 中维护。
     */
    @Override
    public Page<User> queryUser(Page<User> page,
                                String nickname,
                                String username,
                                String phone,
                                String startTime,
                                String endTime) {
        return userMapper.queryUser(page, nickname, username, phone, startTime, endTime);
    }
}
