package com.dmswide.nowcoder.service;

import com.dmswide.nowcoder.entity.Comment;

import java.util.List;

public interface CommentService {
    // TODO: 2022/10/26 dmsWide 封装dao层的方法
    List<Comment> findCommentsByEntity(Integer entityType,Integer entityId,Integer offset,Integer limit);

    // TODO: 2022/10/26 dmsWide
    int findCommentCountByEntity(Integer entityType,Integer entityId);

    // TODO: 2022/10/27 dmsWide 增加评论
    int addComment(Comment comment);

    // TODO: 2022/11/3 dmsWide 根据id查询Comment
    Comment findCommentById(Integer id);

}
