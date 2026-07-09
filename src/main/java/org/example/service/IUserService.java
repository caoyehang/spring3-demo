package org.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.Vo.ApiRest;
import org.example.entity.User;

/**
 * 用户服务接口。
 */
public interface IUserService extends IService<User> {
    /**
     * 登录校验成功后返回 JWT token。
     */
    ApiRest<String> login(String username, String password, String uuid, String code);

    /**
     * 按条件分页查询用户。
     */
    Page<User> queryUser(Page<User> page,
                         String nickname,
                         String username,
                         String phone,
                         String startTime,
                         String endTime);
}
