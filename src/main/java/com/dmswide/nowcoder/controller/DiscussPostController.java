package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.Comment;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.Page;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.CommentService;
import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.service.impl.DiscussPostServiceImpl;
import com.dmswide.nowcoder.service.impl.UserServiceImpl;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Resource
    private DiscussPostServiceImpl discussPostService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private CommentService commentService;
    @Resource
    private LikeService likeService;

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
     * @param page 用来支持分页条件
     * @return 返回模板
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") Integer discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        //作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        // TODO: 2022/10/31 dmsWide 帖子的点赞有关的信息
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);

        //点赞状态:先判断用户是否登录在去查询点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
            likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        // TODO: 2022/10/25 dmsWide 帖子的回复功能还没有实现 评论的分页信息 设置实体类page

        //设置每页评论为五条 这五条是帖子的评论 不是回复的条数为五条
        page.setLimit(5);
        //这句话当时bug好多
        page.setPath("/discuss/detail/" + discussPostId);
        //帖子的总评论数 从discuss_post中取出数据
        page.setRows(discussPost.getCommentCount());

        //评论是专门只帖子的评论 回复是指给评论进行星回复
        //针对的实体是帖子 实体类型是:1

        //评论VO列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                //一个评论的VO
                Map<String,Object> commentVo = new HashMap<>();
                //一条评论
                commentVo.put("comment",comment);
                //评论的作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                // TODO: 2022/10/31 dmsWide 评论的点赞有关的信息
                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);

                //点赞状态:先判断用户是否登录在去查询点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                    likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);

                //查询回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复的VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply : replyList){
                        Map<String,Object> replyVo = new HashMap<>();
                        //存回复
                        replyVo.put("reply",reply);
                        //回复的作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复的目标
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",targetUser);

                        // TODO: 2022/11/1 dmsWide 回复的点赞相关的信息
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);

                        //点赞状态:先判断用户是否登录在去查询点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                            likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                //把回复放进去
                commentVo.put("replies",replyVoList);

                //回复的数量
                int replyCount = commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);
        return "site/discuss-detail";
    }

}
