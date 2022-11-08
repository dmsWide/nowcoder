package com.dmswide.nowcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/*配置类*/
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
