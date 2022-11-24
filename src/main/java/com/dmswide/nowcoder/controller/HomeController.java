package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.Page;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.service.UserService;
import com.dmswide.nowcoder.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;
    @Resource
    private LikeService likeService;

    @GetMapping("/")
    public String root(){
        return "forward:/index";
    }
    @GetMapping("/index")
    public String getHomePage(Model model, Page page,
                              @RequestParam(name = "orderMode",defaultValue = "0") Integer orderMode){
        //隐含逻辑 需要注意的是 在调用方法之前spring mvc会自动创建和实例化方法参数(这里是model和page) 并且会将page注入到model
        //也就是自动完成model.addAttribute("page",page);
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null){
            list.forEach(post->{
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                // TODO: 2022/10/31 dmsWide 增加首页查询帖子或者回复的点赞数的功能
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            });
        }
        model.addAttribute("discussPosts",discussPosts);
        // TODO: 2022/11/15 dmsWide 将orderMode传给模板
        model.addAttribute("orderMode",orderMode);
        //这里写成 "/index" 会报错 templates//index
        return "index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }
}
