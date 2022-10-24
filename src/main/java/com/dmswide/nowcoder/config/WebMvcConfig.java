package com.dmswide.nowcoder.config;

import com.dmswide.nowcoder.controller.interceptor.AlphaInterceptor;
import com.dmswide.nowcoder.controller.interceptor.LoginTicketInterceptor;
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
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
                .addPathPatterns("/login","/register");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
