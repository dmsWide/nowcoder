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
}
