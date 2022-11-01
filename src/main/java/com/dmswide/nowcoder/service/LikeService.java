package com.dmswide.nowcoder.service;

public interface LikeService {
    // TODO: 2022/10/31 dmsWide 点赞
    void like(Integer userId,Integer entityType,Integer entityId);

    // TODO: 2022/10/31 dmsWide 被点赞的次数
    long findEntityLikeCount(Integer entityType,Integer entityId);

    // TODO: 2022/10/31 dmsWide 用户对实体的点赞状态
    int findEntityLikeStatus(Integer userId,Integer entityType,Integer entityId);
}
