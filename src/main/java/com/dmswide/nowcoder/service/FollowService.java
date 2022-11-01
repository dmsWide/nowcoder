package com.dmswide.nowcoder.service;

public interface FollowService {
    // TODO: 2022/11/1 dmsWide 关注
    void follow(Integer userId,Integer entityType,Integer entityId);

    // TODO: 2022/11/1 dmsWide 取关
    void unFollow(Integer userId,Integer entityType,Integer entityId);

    // TODO: 2022/11/1 dmsWide 查询关注了某个实体的数量
    long findFolloweeCount(Integer userId,Integer entityType);

    // TODO: 2022/11/1 dmsWide 查询实体的粉丝数量
    long findFollowerCount(Integer entityType,Integer entityId);

    // TODO: 2022/11/1 dmsWide 查询当前用户是否已经关注该实体
    boolean hasFollowed(Integer userId,Integer entityType,Integer entityId);
}
