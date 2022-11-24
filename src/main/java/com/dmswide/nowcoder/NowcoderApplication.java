package com.dmswide.nowcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/*配置类*/
// TODO: 2022/11/23 dmsWide 这个是Spring Boot的默认启动文件 是当作一个普通的java程序来启动的
//当打成war包 放进tomcat服务器作为web项目来启动时 需要对其进行包装
//tomcat是java程序本身就拥有一个main方法 这个项目里又存在一个项目main方法 所以这样会存在问题
//需要给tomcat提供一个运行这个项目的入口 重写SpringBootServletInitializer 的 configure方法 来运行这个项目
@SpringBootApplication
public class NowcoderApplication {
    //设置elasticsearch不进行netty检查 构造器调用完之后执行 初始化方法
    @PostConstruct
    public void init() {
        // 解决netty启动冲突的问题
        // 在Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }
    public static void main(String[] args) {
        SpringApplication.run(NowcoderApplication.class, args);
    }

}
