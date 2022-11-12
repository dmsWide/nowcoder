package com.dmswide.nowcoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
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

    //HyperLogLog数据结构的使用
    @Test
    public void testHyperLogLog(){
        Random random = new Random();
        //shift + f6 批量修改变量名
        String redisKey = "test:hll:01";
        for(int i = 0;i < (int)(1e5);i++){
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for(int i = 0;i < (int)(1e5);i++){
            redisTemplate.opsForHyperLogLog().add(redisKey,random.nextInt((int)(1e5)));
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    //合并三组数据，将合并后的数据再进行统计独立数据的个数
    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for(int i = 0;i < 1000;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:03";
        for(int i = 100;i < 1100;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:04";
        for(int i = 300;i < 1300;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }
        String redisUnionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(redisUnionKey,redisKey2,redisKey3,redisKey4);

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisUnionKey));
    }

    //统计一组数据的布尔值
    @Test
    public void testBitMap(){
        String redisKey5 = "test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey5,1,true);
        redisTemplate.opsForValue().setBit(redisKey5,3,true);
        redisTemplate.opsForValue().setBit(redisKey5,5,true);
        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey5,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey5,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey5,6));
        //统计
        Long count = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                //统计byte数组中1的个数
                return redisConnection.bitCount(redisKey5.getBytes());
            }
        });
        System.out.println(count);
    }

    //统计三组数据 并对三组数据做or运算
    @Test
    public void testBitMapOperation(){
        String redisKey = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey,0,true);
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,2,true);

        String redisKey1 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey1,2,true);
        redisTemplate.opsForValue().setBit(redisKey1,3,true);
        redisTemplate.opsForValue().setBit(redisKey1,4,true);

        String redisKey2 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey2,4,true);
        redisTemplate.opsForValue().setBit(redisKey2,5,true);
        redisTemplate.opsForValue().setBit(redisKey2,6,true);

        String redisKey3 = "test:bm:or";
        Long count = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                    redisKey3.getBytes(), redisKey.getBytes(), redisKey1.getBytes(), redisKey2.getBytes());
                return redisConnection.bitCount(redisKey3.getBytes());
            }
        });
        System.out.println(count);
        for(int i = 0;i <= 6;i++){
            System.out.println(redisTemplate.opsForValue().getBit(redisKey3,i));
        }
    }
}
