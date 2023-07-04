package com.dmswide.nowcoder.controller.interceptor;

import com.dmswide.nowcoder.entity.LoginTicket;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.impl.UserServiceImpl;
import com.dmswide.nowcoder.util.CookieUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Resource
    private UserServiceImpl userService;
    @Resource
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket != null){
            //根据ticket去redis查询对应的用户信息
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //loginTicket.getStatus() == 0表示凭证有效
            //loginTicket.getExpired().after(new Date())表示过期时间在当前时间之后
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){

                //根据cookie中的key=”ticket“->凭证ticket字符串->根据ticket获取LoginTicket对象->userId->redis中去查询用户信息
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.setUser(user);

                // TODO: 2022/11/10 dmsWide 构建用户认证的结果 存入SecurityContextHolder 用于security授权 在postHandle()方法中需要进行清理
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user,user.getPassword(),userService.getAuthorities(user.getId())
                );

                //SecurityContextHolder是SpringSecurity最基本的组件
                //是用来存放SecurityContext的对象，默认是使用ThreadLocal实现的，保证本线程内所有的方法都可以获得SecurityContext对象
                //在SecurityContextHolder中保存的是当前访问者的信息。Spring Security使用一个Authentication对象来表示这个信息
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            //添加loginUser到Model对象中
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        hostHolder.clear();

        // TODO: 2022/11/10 dmsWide 清除SecurityContextHolder中的数据
        SecurityContextHolder.clearContext();

    }
}
