package com.dmswide.nowcoder.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    //关注
    private static final String PREFIX_FOLLOWEE = "followee";
    //粉丝
    private static final String PREFIX_FOLLOWER = "follower";
    //验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //登录凭证
    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    // TODO: 2022/11/11 dmsWide 统计uv和dau
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

    // TODO: 2022/11/15 dmsWide 将帖子分数修改的帖子放进缓存中 到定时任务执行时计算全部的分数 来决定帖子的排序
    private static final String PREFIX_POST = "post";

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


    /**
     * 登录验证码
     * @param owner 随机字符串来验证登陆者的身份
     * @return 生成redis登录的key
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //获取登录的凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //user
    public static String getUserKey(Integer userId){
        return PREFIX_USER + SPLIT + userId;
    }

    // TODO: 2022/11/11 dmsWide 单日UV
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    // TODO: 2022/11/11 dmsWide 区间UV
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // TODO: 2022/11/11 dmsWide 单日DAU
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    // TODO: 2022/11/11 dmsWide 区间DAU
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // TODO: 2022/11/15 dmsWide 生成存储分数修改的帖子id的redisKey
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
