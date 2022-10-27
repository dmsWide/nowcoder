package com.dmswide.nowcoder.service;

import com.dmswide.nowcoder.dao.AlphaDao;
import com.dmswide.nowcoder.dao.DiscussPostMapper;
import com.dmswide.nowcoder.dao.UserMapper;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Date;

@Service
//不是创建容器的时候就创建实例了 延迟创建在使用对象的时候再创建对象
//@Scope("prototype")
public class AlphaService {

    @Resource
    //@Qualifier("alphaDaoMybatisImpl")
    private AlphaDao alphaDao;

    @Resource
    private UserMapper userMapper;
    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    public AlphaService() {
        System.out.println("construct");
    }

    /**
     * 构造器之后执行
     */
    @PostConstruct
    public void init(){
        System.out.println("after construct");
    }

    /**
     * 销毁对象之前调用
     */

    @PreDestroy
    public void destory(){
        System.out.println("before destory");
    }

    public String find(){
        return alphaDao.select();
    }

    /**
     * 声明式事务(优先选择)
     * 事务传播机制:业务方法A 调用业务方法B B的事务执行是以谁为基准的 涉及两个事务的交叉
     *
     * REQUIRED:支持当前事务(外部事务 调用者事务)A 调用 B这里的外部事务就是A,外部事务不存在就创建外部事务
     * REQUIRES_NEW:创建一个新的事务暂停当前事务(外部事务)
     * NESTED:如果存在当前事务(外部事务),则嵌套在外部事务中执行,A调用B但是B有独立的提交和回滚,外部事务不存在和REQUIRED一样
     * @return
     */

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save(){
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //新增帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hello");
        discussPost.setContent("新人报道");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

        //这里会报错 来测试事务
        //测试结果就是:有错误 插入的数据会被回滚 保证事务的原子性
        int res = Integer.parseInt("abc");
        System.out.println(res);
        return "ok";
    }

    /**
     * 编程式事务:保证回滚
     * @return
     */
    public Object save1(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>(){
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user1 = new User();
                user1.setUsername("beta");
                user1.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user1.setPassword(CommunityUtil.md5("1234" + user1.getSalt()));
                user1.setEmail("beta@qq.com");
                user1.setHeaderUrl("http://image.nowcoder.com/head/199t.png");
                user1.setCreateTime(new Date());
                userMapper.insertUser(user1);
                //新增帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user1.getId());
                discussPost.setTitle("你好");
                discussPost.setContent("新人前来,你们好");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);

                //这里会报错 来测试事务
                //测试结果就是:有错误 插入的数据会被回滚 保证事务的原子性
                int res = Integer.parseInt("abc");
                System.out.println(res);
                return "ok";
            }
        });
    }
}
