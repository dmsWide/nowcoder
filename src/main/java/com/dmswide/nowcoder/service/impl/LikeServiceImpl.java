package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LikeServiceImpl implements LikeService {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     *
     * @param userId 点赞用户Id
     * @param entityType 实体类型
     * @param entityId 实体Id
     * @param entityUserId 被赞用户 因为需要得到被赞用户的总的被赞数量
     */
    //点赞
    @Override
    public void like(Integer userId, Integer entityType, Integer entityId,Integer entityUserId) {
        //执行两次修改redis的操作 使用redis的事务
        //这里的泛型 不知道该怎么设置 存在raw use
        redisTemplate.execute(new SessionCallback(){
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //当前实体类型和实体id生成redisKey
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);

                //被赞用户id形成redis中的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                //判断当前点在的用户是否已经存在点赞的实体的set中
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();
                if(isMember != null){
                    if(isMember){
                        //如果已经点赞,从点赞集合中删除点赞人id
                        operations.opsForSet().remove(entityLikeKey,userId);
                        //点赞数量减一
                        operations.opsForValue().decrement(userLikeKey);
                    }else{
                        //没有点赞 向集合中加入点赞人的id
                        operations.opsForSet().add(entityLikeKey,userId);
                        //点赞数量加一
                        operations.opsForValue().increment(userLikeKey);
                    }
                }
                return operations.exec();
            }
        });
    }

    //查询实体点赞的方法
    @Override
    public long findEntityLikeCount(Integer entityType, Integer entityId) {
        //点赞人数有多少 就是点了多少个赞
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Long size = redisTemplate.opsForSet().size(entityLikeKey);
        if(size != null){
            return size;
        }
        return 0;
    }

    //某位用户对，某个实体的点赞状态:点赞或者未点赞
    @Override
    public int findEntityLikeStatus(Integer userId, Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(isMember != null){
            return  isMember ? 1 : 0;
        }
        //isMember应该不会为null 基本类型的包装类总是提示unboxing时会产生空指针异常
        return -1;
    }

    //查询某个用户获得赞的数量
    public int findUserLikeCount(Integer userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer)redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}
