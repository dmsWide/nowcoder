package com.dmswide.nowcoder.config;

import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
            .antMatchers(
                "/user/setting",
                "/user/upload",
                "/discuss/add",
                "/comment/add/**",
                "/letter/**",
                "/notice/**",
                "/like",
                "/follow",
                "/unfollow"
            ).hasAnyAuthority(AUTHORITY_USER,AUTHORITY_ADMIN,AUTHORITY_MODERATOR)
            .anyRequest().permitAll()
            .and().csrf().disable();

        //没有权限,security捕获到 权限不够 抛出异常的处理 权限不够时的处理
        http.exceptionHandling()
            .authenticationEntryPoint(new AuthenticationEntryPoint() {
                // TODO: 2022/11/10 dmsWide 没有登录时的处理
                @Override
                public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                    //判断请求是异步请求还是同步请求
                    String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                    if("XMLHttpRequest".equals(xRequestedWith)){
                        httpServletResponse.setContentType("application/plain;charset=utf-8");
                        //返回json字符串
                        httpServletResponse.getWriter().write(CommunityUtil.getJSONString(403,"你还未登录"));
                    }else {
                        //返回html页面
                        httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                    }
                }
            })
            .accessDeniedHandler(new AccessDeniedHandler() {
            // TODO: 2022/11/10 dmsWide 权限不足时的处理
            @Override
            public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                if("XMLHttpRequest".equals(xRequestedWith)){
                    httpServletResponse.setContentType("application/plain;charset=utf-8");
                    httpServletResponse.getWriter().write(CommunityUtil.getJSONString(403,"你没有访问该功能的权限"));
                }else {
                    //权限不足 没必要再登录一次因为登陆了还是没权限 跳到404错误页面
                    httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                }
            }
        });

        //security底层在系统退出时会默认自动拦截 /logout请求进行推出处理
        //会在DispatcherServlet之前 也就是在处理器方法执行之前
        //而且执行后 后面的逻辑不会再进行 原来写好的就没用了 所以需要配置不使用security的/logout拦截
        //让security 拦截一个根本就不会出现的路径
        http.logout().logoutUrl("/security_logout");
    }
}
