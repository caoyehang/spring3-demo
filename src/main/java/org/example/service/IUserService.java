package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.Vo.ApiRest;
import org.example.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author 曹业航
 * @since 2025-10-25
 */
public interface IUserService extends IService<User> {
    /**
     * 登录
     */
    public ApiRest login(String username, String password, String uuid, String code);
    /**
     * 分页查询
     */
    Page<User> queryUser(Page page,String nickname ,String username,String phone,String startTime,String endTime);
}
