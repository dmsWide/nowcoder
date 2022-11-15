package com.dmswide.nowcoder;

import com.dmswide.nowcoder.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class ThreadPoolTest {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    //jdk普通的线程池 ExecutorService 使用工厂来创建对象
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    //jdk执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Resource
    private AlphaService alphaService;
    private void sleep(long s){
        try {
            Thread.sleep(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //jdk普通线程池,注意application.yml的输出日志级别需要修改成debug级别的否则看不到输出
    @Test
    public void testExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ExecutorService");
            }
        };
        for(int i = 0;i < 10;i++){
            //线程池分配一个线程来执行这个线程体
            executorService.submit(task);
        }
        //执行test方法的线程sleep 以防止submit线程还未执行完test方法线程就结束的情况
        sleep(3000);
    }

    //jdk 定时任务线程池
    @Test
    public void testScheduledExecutorService(){
        Runnable task = new Runnable(){
            @Override
            public void run() {
                logger.info("Hello ScheduledExecutorService");
            }
        };
        for(int i = 0;i < 10;i++){
            //延时5秒开始执行 每隔1秒输出一次
            scheduledExecutorService.scheduleAtFixedRate(task,5000,1000, TimeUnit.MILLISECONDS);
        }
        sleep(10000);
    }

    //spring普通任务线程池
    @Test
    public void testThreadPoolTaskExecutor(){
         Runnable task = new Runnable(){
            @Override
            public void run() {
                logger.info("Hello ThreadPoolTaskExecutor");
            }
         };

         for (int i = 0;i < 10;i++){
             threadPoolTaskExecutor.submit(task);
         }
         sleep(3000);
    }

    //spring定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("Hello ThreadPoolTaskScheduler");
            }
        };

        Date startTime = new Date(System.currentTimeMillis() + 3000);
        threadPoolTaskScheduler.scheduleAtFixedRate(task,startTime,1000);
        //延时尽量久一点 否则容易提前结束了 上面的info信息来不及输出
        sleep(10000);
    }

    //spring普通线程池的简化使用
    @Test
    public void testThreadPoolTaskExecutorSimple(){
        for(int i = 0;i < 10;i++){
            alphaService.execute();
        }
        sleep(10000);
    }

    @Test
    public void testThreadPoolTaskSchedulerSimple(){
        //@Scheduled注解的方法会默认被调用 不需要主动去调，只需要设置sleep方法即可
        sleep(10000);
    }
}
