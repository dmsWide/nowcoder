package com.dmswide.nowcoder.dao;

import com.dmswide.nowcoder.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    // TODO: 2022/10/26 dmsWide 根据实体来查询 实体包括帖子或者评论(帖子的评论和评论的评论) 还需要包含分页部分 起始行为offset + 1 limit每页显示的行数限制

    List<Comment> selectCommentsByEntity(@Param("entityType") Integer entityType,
                                         @Param("entityId") Integer entityId,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    // TODO: 2022/10/26 dmsWide 评论的条目数
    int selectCommentCountByEntity(@Param("entityType") Integer entityType,@Param("entityId") Integer entityId);

    // TODO: 2022/10/27 dmsWide 添加评论
    int insertComment(Comment comment);

    Comment selectCommentById(@Param("id") Integer id);
}
