package com.dmswide.nowcoder.dao;

import com.dmswide.nowcoder.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // TODO: 2022/10/13 dmsWide 需要使用动态sql 查询每个用户或者全部的帖子
    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId,@Param("offset") Integer offset,@Param("limit") Integer limit);

    // TODO: 2022/10/13 dmsWide
    int selectDiscussPostRows(@Param("userId") Integer userId);

    // TODO: 2022/10/25 dmsWide 增加帖子
    /*没有使用@Param注解 那么sql语句直接写[对象的属性]即可*/
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(@Param("id") int id);

    // TODO: 2022/10/27 dmsWide 更新帖子的评论数量
    int updateCommentCount(@Param("discussPostId") Integer discussPostId,@Param("commentCount")Integer commentCount);
}
