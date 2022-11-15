package com.dmswide.nowcoder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

//启动定时任务一定需要@EnableScheduling注解，因为默认是不启用的
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
