package com.dmswide.nowcoder.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

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
}
