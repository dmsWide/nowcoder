package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.Page;
import com.dmswide.nowcoder.service.ElasticsearchService;
import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.service.UserService;
import com.dmswide.nowcoder.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Resource
    private UserService userService;
    @Resource
    private LikeService likeService;
    @Resource
    private ElasticsearchService elasticsearchService;

    //search?keyword=xxx
    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) throws Exception {
        //搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
            elasticsearchService.searchDiscussPost(keyword,page.getCurrent() - 1, page.getLimit());

        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(searchResult != null){
            for(DiscussPost post : searchResult){
                Map<String,Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //作者
                map.put("user",userService.findUserById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        //分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int)searchResult.getTotalElements());

        return "/site/search";
    }
}

