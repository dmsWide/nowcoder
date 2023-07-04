package com.dmswide.nowcoder.util;

import com.dmswide.nowcoder.entity.User;
import org.springframework.stereotype.Component;

/**
 * 容器作用 用来存储用户信息的 代替的是session对象的
 */
@Component
public class HostHolder {
    //存的是各个线程的user对象
    //ThreadLocal就是存放和取出Bean的容器
    private final ThreadLocal<User> users = new ThreadLocal<>();
    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
