package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author 曹业航
 * @since 2025-10-25
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 分页查询
     */
    Page<User> queryUser(Page page,
                         @Param("nickname") String nickname,
                         @Param("username") String username,
                         @Param("phone") String phone,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);
}
