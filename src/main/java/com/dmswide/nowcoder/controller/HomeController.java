package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.Page;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;

    @GetMapping("/index")
    public String getHomePage(Model model, Page page){
        //隐含逻辑 需要注意的是 在调用方法之前spring mvc会自动创建和实例化方法参数(这里是model和page) 并且会将page注入到model
        //也就是自动完成model.addAttribute("page",page);
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null){
            list.forEach(post->{
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            });
        }
        model.addAttribute("discussPosts",discussPosts);
        //这里写成 "/index" 会报错 templates//index
        return "index";
    }
}
