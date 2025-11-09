package org.example.controller;

import com.google.code.kaptcha.Producer;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Vo.ApiRest;
import org.example.entity.User;
import org.example.service.impl.UserServiceImpl;
import org.example.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * 作者：Leo
 * 描述：永无bug
 */
@RestController
@RequestMapping("/user")
@CrossOrigin // 解决跨域
public class UserController {
    @Autowired
    private Producer producer;
    @Autowired
    public RedisUtils redisUtils;
    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/captchaImage")
    public ApiRest getCode(HttpServletResponse response) throws IOException {
        // 生成文字验证码
        String verifyCode = producer.createText();
        // 生成图片验证码
        BufferedImage image = producer.createImage(verifyCode);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String base64 = cn.hutool.core.codec.Base64.encode(os.toByteArray());
        // 生成随机的没有横线的uuid
        String verifyKey = UUID.randomUUID().toString().replace("-", "");
        // 存储redis里面 并且设置为过期时间为3分钟，时间颗粒为分钟
        redisUtils.set(verifyKey, verifyCode, 180);
        HashMap map = new HashMap();
        try {
            map.put("uuid", verifyKey);
            map.put("img", base64);
            return ApiRest.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiRest.failure(e.getMessage());
        }
    }

    // 登录
    @GetMapping("login")
    public ApiRest login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("uuid") String uuid,
            @RequestParam("code") String code) {
        return userService.login(username, password, uuid, code);
    }

    // 列表
    @PostMapping("list")
    public ApiRest list(
            @RequestParam(defaultValue = "1") Long pageIndex,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        Page<User> page = new Page(pageIndex, pageSize);
        Page<User> userPage = userService.queryUser(page, nickname,username, phone, startTime, endTime);
        return ApiRest.success(userPage);
    }
}
