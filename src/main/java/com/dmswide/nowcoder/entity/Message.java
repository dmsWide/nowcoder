package com.dmswide.nowcoder.entity;

import java.util.Date;

public class Message {
    private Integer id = 0;
    private Integer fromId = 0;
    private Integer toId = 0;
    private String conversationId;
    private String content;
    private Integer status = 0;
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
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
        return "Message{" +
            "id=" + id +
            ", fromId=" + fromId +
            ", toId=" + toId +
            ", conversationId='" + conversationId + '\'' +
            ", content='" + content + '\'' +
            ", status=" + status +
            ", createTime=" + createTime +
            '}';
    }
}
