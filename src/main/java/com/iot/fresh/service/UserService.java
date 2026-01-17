package com.iot.fresh.service;

import com.iot.fresh.dto.UserDto;
import com.iot.fresh.entity.User;

/**
 * 用户服务接口
 * 定义用户信息管理相关业务方法
 * 
 * @author donghuang
 * @since 2026
 */
public interface UserService {
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return UserDto 用户信息数据传输对象
     * @author donghuang
     * @since 2026
     */
    UserDto getUserByUsername(String username);

    /**
     * 更新用户信息
     * 
     * @param userDto 用户信息数据传输对象
     * @return boolean 更新结果
     * @author donghuang
     * @since 2026
     */
    boolean updateUser(UserDto userDto);

    /**
     * 修改用户密码
     * 
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return boolean 修改结果
     * @author donghuang
     * @since 2026
     */
    boolean changePassword(String username, String oldPassword, String newPassword);
}