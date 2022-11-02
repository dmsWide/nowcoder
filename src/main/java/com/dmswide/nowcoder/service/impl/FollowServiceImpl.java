package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.FollowService;
import com.dmswide.nowcoder.service.UserService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class FollowServiceImpl implements FollowService, CommunityConstant {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserService userService;

    /**
     * 对粉丝和关注目标都需要修改,需要启用redis事务
     * @param userId 用户id
     * @param entityType 实体类型
     * @param entityId 实体id
     */
    @Override
    public void follow(Integer userId, Integer entityType, Integer entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                //添加
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    @Override
    public void unFollow(Integer userId, Integer entityType, Integer entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                //删除
                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerKey,userId);
                return operations.exec();
            }
        });
    }

    @Override
    public long findFolloweeCount(Integer userId, Integer entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Long count = redisTemplate.opsForZSet().zCard(followeeKey);
        return count == null ? 0 : count;
    }

    @Override
    public long findFollowerCount(Integer entityType, Integer entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        Long count = redisTemplate.opsForZSet().zCard(followerKey);
        return count == null ? 0 : count;
    }

    //查到分数就是关注了 为空就是没关注
    @Override
    public boolean hasFollowed(Integer userId, Integer entityType, Integer entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }

    @Override
    public List<Map<String, Object>> findFollowee(Integer userId, Integer offset, Integer limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if(targetIds == null){
            return null;
        }
        //不为空转换为对应的返回值类型
        List<Map<String,Object>> list = new ArrayList<>();
        targetIds.forEach(targetId -> {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById((Integer) targetId);
            map.put("user",user);
            //查询关注的时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            if(score == null){
                score = 0.0;
            }
            map.put("followTime",new Date(score.longValue()));

            list.add(map);
        });
        return list;
    }

    @Override
    public List<Map<String, Object>> findFollower(Integer userId, Integer offset, Integer limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if(targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        targetIds.forEach(targetId ->{
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById((Integer) targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            if(score == null){
                score = 0.0;
            }
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        });
        return list;
    }
}
