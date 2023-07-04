package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.UserMapper;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.UserService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.MailClient;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {
    @Resource
    private UserMapper userMapper;
    @Resource
    private MailClient mailClient;
    @Resource
    private TemplateEngine templateEngine;
    /*@Resource
    private LoginTicketMapper loginTicketMapper;*/
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Value("${nowcoder.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User findUserById(Integer userId) {
        //return userMapper.selectById(userId);
        User user = getUserFromCache(userId);
        if(user == null){
            user = initCache(userId);
        }
        return user;
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        if(user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空");
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
        }

        //检查：用户名或者邮箱是否已经注册
        User user1 = userMapper.selectByName(user.getUsername());
        User user2 = userMapper.selectByEmail(user.getEmail());
        if(user1 != null && user2 != null){
            map.put("usernameMsg","用户名已注册");
            map.put("emailMsg","邮箱已注册");
            return map;
        }else if(user1 != null){
            map.put("usernameMsg","用户名已注册");
            return map;
        }else if(user2 != null){
            map.put("emailMsg","邮箱已注册");
            return map;
        }

        //legal username|password|email
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        //用户类型为普通用户
        user.setType(0);
        //账号还未激活
        user.setStatus(0);
        //设置激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setCreateTime(new Date());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1001)));

        userMapper.insertUser(user);

        //发送html激活邮件
        //设置数据
        Context context = new Context();
        context.setVariable("email",user.getEmail());

        //设置网页的激活链接地址
        //url格式:http://localhost/8080/community/activation/userId/activationCode
        //userId是mybatis生成的(配置了use-generated-keys和mapper的keyProperty="id"),用户没有携带这个id的
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        //注册之后跳转到激活
        context.setVariable("url",url);
        //使用模板引擎生成邮件内容
        String text = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活牛客账号",text);

        //空map
        return map;
    }

    /**
     *
     * @param userId 需要激活的用户
     * @param code 激活码
     * @return 激活的状态
     */
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            //已经激活 返回重复激活的提示信息
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            //激活码和数据库中的一致 说明可以激活 则激活账号
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            //返回激活成功
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }
}
