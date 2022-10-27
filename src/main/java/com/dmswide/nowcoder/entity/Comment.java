package com.dmswide.nowcoder.entity;

import java.util.Date;

public class Comment {
    private Integer id = 0;
    private Integer userId = 0;
    private Integer entityType = 0;
    private Integer entityId = 0;
    private Integer targetId = 0;
    private String content;
    private Integer status = 0;
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getEntityType() {
        return entityType;
    }

    public void setEntityType(Integer entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Comment{" +
            "id=" + id +
            ", userId=" + userId +
            ", entityType=" + entityType +
            ", entityId=" + entityId +
            ", targetId=" + targetId +
            ", content='" + content + '\'' +
            ", status=" + status +
            ", createTime=" + createTime +
            '}';
    }
}
