package com.dmswide.nowcoder;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class CaffeineTest {
    @Resource
    private DiscussPostService discussPostService;

    @Test
    public void testInitDate(){
       for(int i = 0;i < 300;i++){
           DiscussPost post = new DiscussPost();
           post.setUserId(111);
           post.setTitle("互联网求职暖春计划");
           post.setContent("今年就业形式非常严峻，过了个年仿佛就像跳水了一样，整个讨论区哀鸿遍野！19届真没人要了么？18届被优化真的很心伤");
           post.setCreateTime(new Date());
           post.setScore(Math.random() * 2000);
           discussPostService.addDiscussPost(post);
       }
    }

    @Test
    public void testCache(){
        //查询三次 应该是访问一次数据库 访问两次Caffeine缓存
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        //走缓存 不需要从数据库中加载数据
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        //orderMode == 0 也不走缓存
        System.out.println(discussPostService.findDiscussPosts(0,0,10,0));
    }
}
