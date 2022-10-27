package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.CommentMapper;
import com.dmswide.nowcoder.entity.Comment;
import com.dmswide.nowcoder.service.CommentService;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.SensitiveWordsFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private SensitiveWordsFilter sensitiveWordsFilter;
    @Resource
    private DiscussPostService discussPostService;
    @Override
    public List<Comment> findCommentsByEntity(Integer entityType, Integer entityId, Integer offset, Integer limit) {
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    @Override
    public int findCommentCountByEntity(Integer entityType, Integer entityId) {
        return commentMapper.selectCommentCountByEntity(entityType,entityId);
    }

    /**
     * 敏感词和html标签的过滤
     * @param comment 评论对象
     * @return
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation= Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveWordsFilter.filter(comment.getContent()));
        //受影响的行数
        int rows = commentMapper.insertComment(comment);

        //更新 帖子 的评论的数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //更新
            int count = commentMapper.selectCommentCountByEntity(comment.getEntityType(), comment.getEntityId());
            //System.out.println("***** count = "+ count +"****");
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }
}
