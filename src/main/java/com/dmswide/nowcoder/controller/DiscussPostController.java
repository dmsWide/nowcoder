package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.impl.DiscussPostServiceImpl;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Resource
    private DiscussPostServiceImpl discussPostService;
    @Resource
    private HostHolder hostHolder;
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"您还未登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        discussPostService.addDiscussPost(discussPost);
        //报错的情况 后续处理
        return CommunityUtil.getJSONString(0,"发帖成功");
    }
}
