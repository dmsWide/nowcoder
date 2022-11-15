package com.dmswide.nowcoder;

import com.dmswide.nowcoder.dao.DiscussPostMapper;
import com.dmswide.nowcoder.dao.UserMapper;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class MapperTest {

    //测试mapper的功能 首先注入mapper
    @Resource
    private UserMapper userMapper;
    //注入discussPostMapper
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectById(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testSelectByName(){
        User user = userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void testSelectByEmail(){
        User user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test_user");
        user.setPassword("123456");
        user.setEmail("test_user@163.com");
        user.setHeaderUrl("https://www.nowcoder.com/101.png");
        user.setSalt("abc");
        user.setCreateTime(new Date());

        int lines = userMapper.insertUser(user);
        System.out.println(lines);
    }

    @Test
    public void testUpdateStatus(){
        int lines = userMapper.updateStatus(151, 1);
        System.out.println(lines);
    }

    @Test
    public void testUpdateHeader(){
        int lines = userMapper.updateHeader(151, "https://www.nowcoder.com/102.png");
        System.out.println(lines);
    }

    @Test
    public void testUpdatePassword(){
        int lines = userMapper.updatePassword(151, "123456789");
        System.out.println(lines);
    }

    @Test
    public void testSelectDiscussPosts(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10,0);
        discussPosts.forEach(System.out::println);
    }

    @Test
    public void testSelectDiscussPostRows(){
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
}
