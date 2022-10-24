package com.dmswide.nowcoder.controller.interceptor;

import com.dmswide.nowcoder.annotation.LoginRequired;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截未登录时的某些请求 所以需要在controller方法之前进行拦截 也就是preHandle
 * 判断登录与否的需要HostHolder
 *
 * 拦截器创建之后需要进行配置(在config包中进行配置) 指定生效的路径:静态资源全部不拦截 请求全部拦截然后判断是否有LoginRequired注解
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Resource
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //有登录要求(有@LoginRequired注解) 但是没有登录
            if(loginRequired != null && hostHolder.getUser() == null){
                //重定向到登录页面 这里跟Controller的区别时这里不能直接返回虚拟路径
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
