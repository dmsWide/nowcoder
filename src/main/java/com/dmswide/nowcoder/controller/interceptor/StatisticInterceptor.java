package com.dmswide.nowcoder.controller.interceptor;

import com.dmswide.nowcoder.service.StatisticService;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class StatisticInterceptor implements HandlerInterceptor {
    @Resource
    private StatisticService statisticService;
    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计UV
        String ip = request.getRemoteHost();
        statisticService.recordUV(ip);
        //统计DAU
        if(hostHolder.getUser() != null){
            statisticService.recordDAU(hostHolder.getUser().getId());
        }
        //注入到WebMvcConfig中进行配置，让拦截器生效
        return true;
    }
}
