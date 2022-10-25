package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.impl.DiscussPostServiceImpl;
import com.dmswide.nowcoder.service.impl.UserServiceImpl;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Resource
    private DiscussPostServiceImpl discussPostService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserServiceImpl userService;
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

    /**
     * 根绝帖子id来查看帖子详情 这里的帖子作者使用的是两次数据库查询的方式 没有使用关联查询
     * @param discussPostId 帖子的id
     * @param model 携带数据给模板
     * @return 返回模板
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") Integer discussPostId,Model model){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        //作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        // TODO: 2022/10/25 dmsWide 帖子的回复功能还没有实现

        return "site/discuss-detail";
    }

}
