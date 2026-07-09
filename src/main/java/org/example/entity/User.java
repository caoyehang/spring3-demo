package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表实体，对应 sys_user。
 */
@TableName("sys_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户主键。
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 登录账号。
     */
    private String username;

    /**
     * 登录密码。
     */
    private String password;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 性别：0 保密，1 男，2 女。
     */
    private Integer sex;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新时间。
     */
    private Date updateTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", nickname=" + nickname +
                ", username=" + username +
                ", password=" + password +
                ", email=" + email +
                ", phone=" + phone +
                ", sex=" + sex +
                ", remark=" + remark +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                "}";
    }
}
