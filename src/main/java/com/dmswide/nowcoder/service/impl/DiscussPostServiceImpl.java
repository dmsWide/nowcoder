package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.DiscussPostMapper;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.util.SensitiveWordsFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private SensitiveWordsFilter sensitiveWordsFilter;
    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit,Integer orderMode) {
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    @Override
    public int findDiscussPostRows(Integer userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Override
    public int addDiscussPost(DiscussPost discussPost) {
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //预处理

        //转义html标记 防止标题中出现 标签元素
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //处理敏感词
        discussPost.setTitle(sensitiveWordsFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveWordsFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost findDiscussPostById(Integer id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int updateCommentCount(Integer discussPostId, Integer commentCount) {
        return discussPostMapper.updateCommentCount(discussPostId,commentCount);
    }

    @Override
    public int updateType(Integer id, Integer type) {
        return discussPostMapper.updateType(id,type);
    }

    @Override
    public int updateStatus(Integer id, Integer status) {
        return discussPostMapper.updateStatus(id,status);
    }

    @Override
    public int updateScore(Integer id, Double score) {
        return discussPostMapper.updateScore(id,score);
    }
}
