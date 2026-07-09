package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.User;

/**
 * 用户表 Mapper。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 按筛选条件分页查询用户。
     */
    Page<User> queryUser(Page<User> page,
                         @Param("nickname") String nickname,
                         @Param("username") String username,
                         @Param("phone") String phone,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);
}
