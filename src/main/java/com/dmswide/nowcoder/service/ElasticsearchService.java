package com.dmswide.nowcoder.service;

import com.dmswide.nowcoder.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticsearchService {

    // TODO: 2022/11/8 dmsWide 保存帖子到es服务器
    void saveDiscussPost(DiscussPost discussPost);

    // TODO: 2022/11/8 dmsWide 根据id删除es服务器上的帖子
    void deleteDiscussPost(int id);

    // TODO: 2022/11/8 dmsWide 分页查询帖子
    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);
}
