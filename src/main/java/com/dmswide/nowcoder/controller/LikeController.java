package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;

@Controller
public class LikeController {
    @Resource
    private LikeService likeService;
    @Resource
    private HostHolder hostHolder;

    @PostMapping("/like")
    @ResponseBody
    public String like(Integer entityType,Integer entityId){
        //这里没有校验用户是否登录 没有登录的话点赞 会有问题
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(),entityType,entityId);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        //用户的点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //返回结果
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        return CommunityUtil.getJSONString(0,null,map);
    }
}
