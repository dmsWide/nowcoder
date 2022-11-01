package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LikeServiceImpl implements LikeService {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //点赞
    @Override
    public void like(Integer userId, Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(isMember != null){
            if(isMember){
                //如果已经点赞,从点赞集合中删除点赞人id
                redisTemplate.opsForSet().remove(entityLikeKey,userId);
            }else{
                //没有点赞 向集合中加入点赞人的id
                redisTemplate.opsForSet().add(entityLikeKey,userId);
            }
        }
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
}
