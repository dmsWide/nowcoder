package com.dmswide.nowcoder.service;

import java.util.List;
import java.util.Map;

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

    // TODO: 2022/11/1 dmsWide 查询某用户关注的人,这里的实体是人,后期查询其他实体的话可以再扩充
    List<Map<String,Object>> findFollowee(Integer userId,Integer offset,Integer limit);

    // TODO: 2022/11/1 dmsWide 查询某用户的粉丝
    List<Map<String,Object>> findFollower(Integer userId,Integer offset,Integer limit);
}
