package com.dmswide.nowcoder.dao;

import com.dmswide.nowcoder.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // TODO: 2022/10/13 dmsWide
    User selectById(@Param("id") Integer id);

    // TODO: 2022/10/13 dmsWide
    User selectByName(@Param("username") String username);

    // TODO: 2022/10/13 dmsWide
    User selectByEmail(@Param("email") String email);

    // TODO: 2022/10/13 dmsWide
    int insertUser(User user);

    // TODO: 2022/10/13 dmsWide
    int updateStatus(@Param("id") Integer id,@Param("status") Integer status);

    // TODO: 2022/10/13 dmsWide
    int updateHeader(@Param("id") Integer id,@Param("headUrl") String headerUrl);

    // TODO: 2022/10/13 dmsWide
    int updatePassword(@Param("id") Integer id,@Param("password") String password);
}
