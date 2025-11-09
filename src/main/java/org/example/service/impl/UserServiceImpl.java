package org.example.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.Vo.ApiRest;
import org.example.entity.User;
import org.example.enums.ApiErrorEnum;
import org.example.mapper.UserMapper;
import org.example.service.IUserService;
import org.example.utils.JwtUtils;
import org.example.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.example.utils.RsaUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.PrivateKey;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author 曹业航
 * @since 2025-10-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // 注入mapper层
    @Autowired
    private UserMapper userMapper;
    @Autowired
    public RedisUtils redisUtils;
    /**
     * 登录
     * @param username
     * @param password
     * @param uuid
     * @param code
     * @return
     */
    @Override
    public ApiRest login(String username, String password, String uuid, String code) {
        // 判断 用户名或者密码是否为空
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_USERNAME_NULL);
        }
        // 判断 uuid或者验证码是否为空
        if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(code)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_CODE_UUID_NULL);
        }
        // 使用mybatis plus快捷查询，根据用户名查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User users = userMapper.selectOne(queryWrapper);
        // 判断查询结果，如果用户名查询不出来
        if (users == null) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_USERNAME_PASSWORD_INVALID);
        }
        // 比较密码 如果密码不正确
        if (!users.getPassword().equals(password)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_USERNAME_PASSWORD_INVALID);
        }
        // 根据uuid查询 验证码
        String verifyKey = redisUtils.get(uuid);
        // 查询不到就是过期
        if (StringUtils.isEmpty(verifyKey)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_UUID_INVALID);
        }
        // 判断验证码是否正确
        if (!verifyKey.equals(code)) {
            return ApiRest.failure(ApiErrorEnum.LOGIN_CODE_INVALID);
        }
        // 全部通过后，颁发jwt令牌
        PrivateKey privateKey = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("key/pri_rsa");
            InputStream is = classPathResource.getInputStream();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int rc;
            while ((rc = is.read(buff, 0, 1024)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            byte[] keyBytes = swapStream.toByteArray();
            // 通过文件路径获取私钥
            privateKey = RsaUtils.getPrivateKey(keyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用私钥加密token 单位 分钟
        String token = JwtUtils.generateTokenExpireInMinutes(users, privateKey, 120);
        // 全部成功返回token
        return ApiRest.success(token);
    }
    /**
     * 登录
     * @param username
     * @param nickname
     * @param phone
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public Page<User> queryUser(Page page, String nickname, String username, String phone, String startTime, String endTime) {
        Page<User> userPage = userMapper.queryUser(page, nickname, username, phone, startTime, endTime);
        return userPage;
    }
}
