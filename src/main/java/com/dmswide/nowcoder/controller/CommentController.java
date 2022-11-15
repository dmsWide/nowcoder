package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.Comment;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.Event;
import com.dmswide.nowcoder.event.EventProducer;
import com.dmswide.nowcoder.service.CommentService;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.HostHolder;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Resource
    private CommentService commentService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") Integer discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        commentService.addComment(comment);

        // TODO: 2022/11/2 dmsWide 触发评论事件
        Event event = new Event()
            .setTopic(TOPIC_COMMENT)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(comment.getEntityType())
            .setEntityId(comment.getEntityId())
            .setData("postId",discussPostId);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);

        // TODO: 2022/11/8 dmsWide 评论帖子的时候 会修改帖子的评论数量修改了帖子 这是还需触发事件 把es中的帖子的数据覆盖更新成最新的
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //触发发帖事件
            event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(comment.getUserId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // TODO: 2022/11/15 dmsWide 评论对象是帖子的时候才需要算分
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }

        //转发和上面的发消息时异步进行的
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
