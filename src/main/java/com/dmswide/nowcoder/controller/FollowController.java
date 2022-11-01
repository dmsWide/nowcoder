package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.FollowService;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class FollowController {
    @Resource
    private FollowService followService;
    @Resource
    private HostHolder hostHolder;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(Integer entityType,Integer entityId){
        User user = hostHolder.getUser();
        if(user == null){
            throw new RuntimeException("未登录");
        }
        followService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已关注!");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unFollow(Integer entityType,Integer entityId){
        User user = hostHolder.getUser();
        if(user == null){
            throw new RuntimeException("未登录");
        }
        followService.unFollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取关");
    }
}
