package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.UserMapper;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.UserService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {
    @Resource
    private UserMapper userMapper;
    @Resource
    private MailClient mailClient;
    @Resource
    private TemplateEngine templateEngine;
    @Resource
    private LoginTicketMapper loginTicketMapper;
    @Value("${nowcoder.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User findUserById(Integer userId) {
        return userMapper.selectById(userId);
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

        user.setType(0);
        user.setStatus(0);
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
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }
}
