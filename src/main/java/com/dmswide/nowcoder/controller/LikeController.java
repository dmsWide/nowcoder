package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.Event;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.event.EventProducer;
import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;

@Controller
public class LikeController implements CommunityConstant {
    @Resource
    private LikeService likeService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(Integer entityType,Integer entityId,Integer entityUserId,Integer postId){
        //这里没有校验用户是否登录 没有登录的话点赞 会有问题
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        //用户的点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //返回结果
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        // TODO: 2022/11/3 dmsWide 触发点赞事件:点赞的时候通知 取消点赞时不需要通知
        if(likeStatus == 1){
            Event event = new Event()
                .setTopic(TOPIC_LIKE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityUserId)
                .setData("postId",postId);
            eventProducer.fireEvent(event);

        }

        // TODO: 2022/11/15 dmsWide 给帖子点赞时需要计算分数
        if(entityType == ENTITY_TYPE_POST){
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }
        return CommunityUtil.getJSONString(0,null,map);
    }
}
