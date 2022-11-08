package com.dmswide.nowcoder.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**和es差生关联*/
@Document(indexName = "discusspost",type = "_doc",shards = 6,replicas = 3)
public class DiscussPost {
    @Id
    private Integer id = 0;

    @Field(type = FieldType.Integer)
    private Integer userId = 0;

    /*两个分词器不同*/
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private Integer type = 0;

    @Field(type = FieldType.Integer)
    private Integer status = 0;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Integer)
    private Integer commentCount = 0;

    @Field(type = FieldType.Double)
    private Double score = 0.0d;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DiscussPost{" +
            "id=" + id +
            ", userId=" + userId +
            ", title='" + title + '\'' +
            ", content='" + content + '\'' +
            ", type=" + type +
            ", status=" + status +
            ", createTime=" + createTime +
            ", commentCount=" + commentCount +
            ", score=" + score +
            '}';
    }
}
