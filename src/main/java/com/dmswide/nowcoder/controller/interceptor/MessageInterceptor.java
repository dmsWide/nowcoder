package com.dmswide.nowcoder.controller.interceptor;

import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.MessageService;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 编写拦截器在模板返回之前进行拦截所以需要实现postHandle方法
 * 并且还需要在WebMvcConfig配置类中进行配置:来设置作用的范围
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Resource
    private HostHolder hostHolder;
    @Resource
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            int lettersUnreadCount = messageService.findLettersUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount",lettersUnreadCount + noticeUnreadCount);
        }
    }
}
