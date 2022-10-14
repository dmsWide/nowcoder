package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.DiscussPostMapper;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.service.DiscussPostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit) {
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    @Override
    public int findDiscussPostRows(Integer userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }
}
