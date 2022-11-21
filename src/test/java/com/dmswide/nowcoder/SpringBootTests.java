package com.dmswide.nowcoder;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class SpringBootTests {
    @Resource
    private DiscussPostService discussPostService;
    private DiscussPost post;

    //只调一次
    @BeforeClass
    public static void beforeClass(){
        System.out.println("beforeClass");
    }
    //只调一次
    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }

    //调用多次 每次测试方法调用时都会调用@Before注解修饰的方法 同时也会调用@After注解修饰的方法
    //这样处理的优点是:不会依赖其他的方法的数据，全部的数据都在自己的方法内部进行了处理
    @Before
    public void before(){
        System.out.println("before");
        post = new DiscussPost();
        post.setUserId(111);
        post.setTitle("test title");
        post.setContent("test content");
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);
    }
    //调用多次
    @After
    public void after(){
        System.out.println("after");
        //修改帖子状态为2 代表删除帖子
        discussPostService.updateStatus(post.getId(),2);
    }

    @Test
    public void testMethodFirst(){
        System.out.println("first test method");
    }

    @Test
    public void testMethodSecond(){
        System.out.println("second test method");
    }

    @Test
    public void testFindById(){
        //在@Before里创建了一个Discusspost的实例 并且在该测试方法执行完之后 执行@After删除(修改帖子状态)
        DiscussPost p = discussPostService.findDiscussPostById(post.getId());
        //使用断言的方式判断查询的到结果是否符合预期
        Assert.assertNotNull(p);
        //前面是expected 后面是actual
        Assert.assertEquals(post.getTitle(),p.getTitle());
        Assert.assertEquals(post.getContent(),p.getContent());
    }

    @Test
    public void testUpdateScore(){
        int rows = discussPostService.updateScore(post.getId(), 2000d);
        Assert.assertEquals(1,rows);
        //判断一下score是否变了
        DiscussPost p = discussPostService.findDiscussPostById(post.getId());
        //判断双精度小数 delta参数是用于判断精度的 这里判断到小数点后2位小数是否相等
        Assert.assertEquals(2000d,p.getScore(),2);
    }
}
