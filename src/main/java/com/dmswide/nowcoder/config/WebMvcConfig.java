package com.dmswide.nowcoder.config;

import com.dmswide.nowcoder.controller.interceptor.AlphaInterceptor;
import com.dmswide.nowcoder.controller.interceptor.LoginRequiredInterceptor;
import com.dmswide.nowcoder.controller.interceptor.LoginTicketInterceptor;
import com.dmswide.nowcoder.controller.interceptor.MessageInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 一般的config配置类第三方的类 拦截器配置类有些特殊
 * 需要实现接口WebMvcConfigurer 来注册拦截器 来进行请求路径的拦截和排除特定的请求路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private AlphaInterceptor alphaInterceptor;
    @Resource
    private LoginTicketInterceptor loginTicketInterceptor;
    @Resource
    private LoginRequiredInterceptor loginRequiredInterceptor;
    @Resource
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
                .addPathPatterns("/login","/register");

        //加入了user
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        //使用user
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        //动态请求都拦截 静态请求全部放行
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
