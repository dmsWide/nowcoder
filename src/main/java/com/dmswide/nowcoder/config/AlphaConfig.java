package com.dmswide.nowcoder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AlphaConfig {
    //引入第三方的类需要使用配置类来实现,返回值类型就是需要装配的类型，方法返回的对象会被装配的容器里
    //bean的名字默认是方法名，可以自己指定bean的名称
    @Bean(value = "simpleDateFormat")
    public SimpleDateFormat getSimpleDateFormat(){
        return  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

}
