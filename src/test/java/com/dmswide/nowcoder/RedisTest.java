package com.dmswide.nowcoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class RedisTest {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void testStrings(){
        String redisKey = "test:count";
        //设置
        redisTemplate.opsForValue().set(redisKey,1);
        //取值
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        //增加
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        //减少
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHashs(){
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey,"id","001");
        redisTemplate.opsForHash().put(redisKey,"username","john");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
    }

    @Test
    public void testLists(){
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey,"101");
        redisTemplate.opsForList().leftPush(redisKey,"102");
        redisTemplate.opsForList().leftPush(redisKey,"103");

        Long size = redisTemplate.opsForList().size(redisKey);
        System.out.println(size);

        String id = (String)redisTemplate.opsForList().index(redisKey, 0);
        System.out.println(id);
        //end索引可以越界 越界索引部分不输出元素[0,2]有元素 但是实际是使用[0，4]并未报错
        List<Object> range = redisTemplate.opsForList().range(redisKey, 0, 4);
        if(range != null){
            range.forEach(System.out::println);
        }

        String ele = (String)redisTemplate.opsForList().leftPop(redisKey);
        System.out.println("ele: " + ele);
        ele = (String)redisTemplate.opsForList().leftPop(redisKey);
        System.out.println("ele: " + ele);
        ele = (String)redisTemplate.opsForList().leftPop(redisKey);
        System.out.println("ele: " + ele);
    }

    @Test
    public void testSets(){
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey,"shanks","shanon","sam","tony");
        Long size = redisTemplate.opsForSet().size(redisKey);
        System.out.println("size: " + size);

        String ele = (String) redisTemplate.opsForSet().pop(redisKey);
        System.out.println(ele);

        Set<Object> members = redisTemplate.opsForSet().members(redisKey);
        if(members != null){
            members.forEach(System.out::println);
        }
    }

    @Test
    public void testSortedSet(){
        String redisKey = "test:students";
        redisTemplate.opsForZSet().add(redisKey,"唐僧",50);
        redisTemplate.opsForZSet().add(redisKey,"孙悟空",90);
        redisTemplate.opsForZSet().add(redisKey,"猪八戒",80);
        redisTemplate.opsForZSet().add(redisKey,"沙悟净",70);
        redisTemplate.opsForZSet().add(redisKey,"白龙马",60);

        Long size = redisTemplate.opsForZSet().size(redisKey);
        System.out.println("size: " + size);
        Long card = redisTemplate.opsForZSet().zCard(redisKey);
        System.out.println("card: " + card);
        //统计某个人的分数
        Double score = redisTemplate.opsForZSet().score(redisKey, "沙悟净");
        System.out.println("score: " + score);
        //统计某个人的分数
        //由小到大 返回的是索引
        Long rank = redisTemplate.opsForZSet().rank(redisKey, "白龙马");
        //由大到小 返回的也是索引
        Long reverseRank = redisTemplate.opsForZSet().reverseRank(redisKey, "白龙马");
        System.out.println("rank: " + rank);
        System.out.println("reverseRank: " + reverseRank);

        //取前三名[0,2]
        Set<Object> range = redisTemplate.opsForZSet().range(redisKey, 0, 2);
        Set<Object> reverseRange = redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2);
        if(range != null){
            System.out.println("由小到大前三名");
            range.forEach(System.out::println);
        }

        if(reverseRange != null){
            System.out.println("由大到小前三名");
            reverseRange.forEach(System.out::println);
        }
    }

    @Test
    public void testDelete(){
        String redisKey = "test:user";
        redisTemplate.delete(redisKey);

        Boolean flag = redisTemplate.hasKey(redisKey);
        System.out.println(flag);

        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);
    }

    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        //BoundValueOperations 和 boundValueOps 说明绑定的是value也就是redis中的string数据结构
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(redisKey);
        //一次性增加5
        operations.increment(5);
        Integer res = (Integer) operations.get();
        System.out.println(res);

    }

    //redis事务：主要使用编程式事务有效缩小事务的执行范围 和mysql刚好相反 在redis事务中启用后查询操作不会立即起作用 所以查询操作应该在事务之前或者事务结束之后执行
    //redis事务在未commit之前会被放入队列 直到commit之后才会被全部执行
    @Test
    public void testTransactional(){
        //接收的数据是内部execute()方法的返回值
        Object obj = redisTemplate.execute(new SessionCallback<Object>() {
            /**
             * 外层execute会调用SessionCallback的内部实现方法execute()
             * @param operations 执行命令的对象 来执行命令管理事务
             * @return 结束事务
             * @throws DataAccessException 异常对象
             */
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String redisKey = "test:tx";
                BoundSetOperations<String, Object> boundSetOperations = redisTemplate.boundSetOps(redisKey);
                //开始redis事务
                operations.multi();

                boundSetOperations.add("tom", "jerry", "marin", "martin", "christ");
                Set<Object> members = boundSetOperations.members();
                if (members != null) {
                    members.forEach(System.out::println);
                }

                //关闭redis事务
                return operations.exec();
            }
        });
        //[5, [jerry, martin, marin, tom, christ]] 5表示执行的命令影响的行数 后面是插入的数据
        System.out.println(obj);
    }
}
