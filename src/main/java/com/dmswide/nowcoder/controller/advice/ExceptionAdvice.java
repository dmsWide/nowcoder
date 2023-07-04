package com.dmswide.nowcoder.controller.advice;

import com.dmswide.nowcoder.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// 是Controller全局配置类，不用对任何Controller再做配置，可以统一做Controller的全局配置。@ControllerAdvice用来修饰类。
// 异常处理方案@ExceptionHandler、绑定数据方案@ModelAttribute、绑定参数方案@DataBinder. 他们都用来修饰方法。
// 这里只演示，统一处理异常（@ExceptionHandler） ControllerAdvice限定处理Controller
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //写在方法上 如果出现异常统统调用这个方法来处理异常
    //Exception.class限定所有异常
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常: " + e.getMessage());
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }

        //判断请求的类型
        // 给浏览器响应
        // 要看是什么请求，想要服务器返回网页html/异步请求JSON(xml).从请求的消息头获取。
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            //提示服务器异常
            writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
        }else{
            //向HomeController中发送请求 控制器方法会处理这个请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
