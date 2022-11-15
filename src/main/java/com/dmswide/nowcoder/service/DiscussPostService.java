package com.dmswide.nowcoder.service;

import com.dmswide.nowcoder.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {
    /**
     * service层实现业务主要是dao层的curd的组合，但是这里只是简单的业务查询 所以跟dao接口一致没有出现方法组合的情况
     */

    // TODO: 2022/10/14 dmsWide
    List<DiscussPost> findDiscussPosts(Integer userId,Integer offset,Integer limit,Integer orderMode);

    // TODO: 2022/10/14 dmsWide
    int findDiscussPostRows(Integer userId);
    // TODO: 2022/10/25 dmsWide
    int addDiscussPost(DiscussPost discussPost);

    // TODO: 2022/10/25 dmsWide
    DiscussPost findDiscussPostById(Integer id);

    int updateCommentCount(Integer discussPostId,Integer commentCount);

    // TODO: 2022/11/11 dmsWide 更新帖子类型
    int updateType(Integer id,Integer type);

    // TODO: 2022/11/11 dmsWide 更新帖子状态
    int updateStatus(Integer id,Integer status);

    // TODO: 2022/11/15 dmsWide 更新帖子分数
    int updateScore(Integer id,Double score);
}
