package com.dmswide.nowcoder;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
//以前的项目是使用Spring Boot内嵌的tomcat跑项目 现在项目打成war包用外部web容器跑
//springboot应用一般打包成jar使用内置容器运行，但是如果想像传统web项目一样打成war包，部署到容器中，启动类的main方法不能够识别
//需要继承SpringBootServletInitializer重写configure方法将其指向应用的启动类，以部署在web容器上 的传统war文件 运行Spring Boot Application
//tomcat启动 自动找到该类运行configure方法 从而运行项目 =>当作一个项目运行入口
public class NowcoderServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        //参数是项目的主配置文件 核心配置文件 然后可以运行项目
        return builder.sources(NowcoderApplication.class);
    }
}
