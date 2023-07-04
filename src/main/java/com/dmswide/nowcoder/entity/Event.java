package com.dmswide.nowcoder.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    //事件类型：点赞 评论（评论帖子或者帖子详情页的评论）关注
    private String topic;
    //触发事件的用户Id
    private Integer userId = 0;
    //事件发生在哪个实体（评论帖子 评论评论 点赞帖子 点赞评论 关注用户 实体类型可以是帖子 评论 或者用户）
    private Integer entityType = 0;
    //实体Id
    private Integer entityId = 0;
    //实体的作者 实体类型有 帖子 用户 评论 实体作者只有对应产生响应实体的用户
    private Integer entityUserId = 0;
    //额外的数据 方便扩展
    private Map<String,Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public Event setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public Integer getEntityType() {
        return entityType;
    }

    public Event setEntityType(Integer entityType) {
        this.entityType = entityType;
        return this;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public Event setEntityId(Integer entityId) {
        this.entityId = entityId;
        return this;
    }

    public Integer getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(Integer entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key,value);
        return this;
    }
}
