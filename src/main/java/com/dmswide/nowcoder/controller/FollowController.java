package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.Event;
import com.dmswide.nowcoder.entity.Page;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.event.EventProducer;
import com.dmswide.nowcoder.service.FollowService;
import com.dmswide.nowcoder.service.UserService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Resource
    private FollowService followService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserService userService;
    @Resource
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(Integer entityType,Integer entityId){
        User user = hostHolder.getUser();
        if(user == null){
            throw new RuntimeException("未登录");
        }
        followService.follow(user.getId(),entityType,entityId);

        // TODO: 2022/11/3 dmsWide 触发关注事件
        Event event = new Event()
            .setTopic(TOPIC_FOLLOW)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(entityType)
            .setEntityId(entityId)
            .setEntityUserId(entityId);

        eventProducer.fireEvent(event);

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

    @GetMapping("/followees/{userId}")
    public String getFollowee(@PathVariable("userId")Integer userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        //设置评论分页
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowee(userId, page.getOffset(), page.getLimit());

        for(Map<String,Object> map : userList){
            User u = (User) map.get("user");
            map.put("hasFollowed",hasFollowed(u.getId()));
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

       @GetMapping("/followers/{userId}")
    public String getFollower(@PathVariable("userId")Integer userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        //设置评论分页
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> userList = followService.findFollower(userId, page.getOffset(), page.getLimit());

        for(Map<String,Object> map : userList){
            User u = (User) map.get("user");
            map.put("hasFollowed",hasFollowed(u.getId()));
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }

    private boolean hasFollowed(Integer userId){
        if(hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }
}
