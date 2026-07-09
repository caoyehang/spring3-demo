package org.example.controller;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.code.kaptcha.Producer;
import org.example.Vo.ApiRest;
import org.example.entity.User;
import org.example.service.IUserService;
import org.example.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户接口。
 * <p>
 * 负责验证码、登录和用户分页查询。所有接口都返回 ApiRest，方便前端统一处理。
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    private final Producer producer;
    private final RedisUtils redisUtils;
    private final IUserService userService;

    @Autowired
    public UserController(Producer producer, RedisUtils redisUtils, IUserService userService) {
        this.producer = producer;
        this.redisUtils = redisUtils;
        this.userService = userService;
    }

    /**
     * 生成图片验证码。
     * <p>
     * 返回 uuid 和图片 base64；登录时前端需要把 uuid、验证码一起提交回来。
     */
    @GetMapping("/captchaImage")
    public ApiRest<Map<String, String>> getCode() throws IOException {
        String verifyCode = producer.createText();
        BufferedImage image = producer.createImage(verifyCode);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);

        String base64 = Base64.encode(outputStream.toByteArray());
        String verifyKey = UUID.randomUUID().toString().replace("-", "");

        // 验证码写入 Redis，180 秒后自动过期。
        redisUtils.set(verifyKey, verifyCode, 180);

        Map<String, String> result = new HashMap<>();
        result.put("uuid", verifyKey);
        result.put("img", base64);
        return ApiRest.success(result);
    }

    /**
     * 用户登录。
     * <p>
     * 保留 GET 兼容当前页面，同时允许 POST，后续前端可以平滑切换到 POST。
     */
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiRest<String> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("uuid") String uuid,
            @RequestParam("code") String code) {
        return userService.login(username, password, uuid, code);
    }

    /**
     * 分页查询用户列表。
     * <p>
     * 该接口会被 Security 拦截，必须携带合法 token。
     */
    @PostMapping("/list")
    public ApiRest<Page<User>> list(
            @RequestParam(defaultValue = "1") Long pageIndex,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<User> page = new Page<>(pageIndex, pageSize);
        Page<User> userPage = userService.queryUser(page, nickname, username, phone, startTime, endTime);
        return ApiRest.success(userPage);
    }
}
