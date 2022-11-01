package com.dmswide.nowcoder.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    //某个实体的赞:帖子或者回复
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(Integer entityType,Integer entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某位用户收到的赞 包括自己发布的帖子和全部评论收到的赞的和
    //like:user:userId -> int
    public static String getUserLikeKey(Integer userId){
        return PREFIX_USER_LIKE + SPLIT +userId;
    }

    //用户关注的实体:不一定是某位用户 可以是某个帖子 问题等等
    //followee:userId(粉丝id):entityType(关注实体类型) -> zset(entityId,(int)now):按照先后顺序列举出来关注
    public static String getFolloweeKey(Integer userId,Integer entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId -> zset(UserId,now):按照时间先后来评分
    public static String getFollowerKey(Integer entityType,Integer entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
}
