package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.UserMapper;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Override
    public User findUserById(Integer userId) {
        return userMapper.selectById(userId);
    }
}
