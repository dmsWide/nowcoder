package com.dmswide.nowcoder.service;

public interface LikeService {
    // TODO: 2022/10/31 dmsWide 点赞
    void like(Integer userId,Integer entityType,Integer entityId,Integer entityUserId);

    // TODO: 2022/10/31 dmsWide 被点赞的次数
    long findEntityLikeCount(Integer entityType,Integer entityId);

    // TODO: 2022/10/31 dmsWide 用户对实体的点赞状态
    int findEntityLikeStatus(Integer userId,Integer entityType,Integer entityId);

    // TODO: 2022/11/1 dmsWide 查询某个用户获得赞的数量
    int findUserLikeCount(Integer userId);
}
