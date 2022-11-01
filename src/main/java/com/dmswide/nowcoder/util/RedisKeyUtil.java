package com.dmswide.nowcoder.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    //某个实体的赞:帖子或者回复
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(Integer entityType,Integer entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}
