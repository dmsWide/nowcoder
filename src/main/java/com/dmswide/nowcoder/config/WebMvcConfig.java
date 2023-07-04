package com.dmswide.nowcoder.config;

import com.dmswide.nowcoder.controller.interceptor.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 一般的config配置类第三方的类 拦截器配置类有些特殊
 * 需要实现接口WebMvcConfigurer来注册拦截器，来进行请求路径的拦截和排除特定的请求路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private AlphaInterceptor alphaInterceptor;
    @Resource
    private LoginTicketInterceptor loginTicketInterceptor;

    // TODO: 2022/11/9 dmsWide 使用spring security来进行拦截 不再使用拦截器 废弃这个拦截器
    /*@Resource
    private LoginRequiredInterceptor loginRequiredInterceptor;*/
    @Resource
    private MessageInterceptor messageInterceptor;
    @Resource
    private StatisticInterceptor statisticInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
                .addPathPatterns("/login","/register");

        //加入了user
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        //使用user
        //registry.addInterceptor(loginRequiredInterceptor)
        //.excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        //动态请求都拦截 静态请求全部放行
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        // TODO: 2022/11/12 dmsWide 同样是不需要拦截静态资源只拦截对动态资源的访问
        registry.addInterceptor(statisticInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
