package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.DiscussPostMapper;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.util.SensitiveWordsFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Resource
    private SensitiveWordsFilter sensitiveWordsFilter;

    //Caffeine本地缓存
    @Value("${caffeine.posts.max-size}")
    private Integer maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private Integer expireSeconds;

    // TODO: 2022/11/17 dmsWide Caffeine的主要接口是Cache主要的子接口是LoadingCaching 和 AsyncLoadingCaching
    //LoadingCache是同步缓存，一般使用LoadingCache
    //AsyncLoadingCache是异步缓存，支持并发的取数据。
    //一个缓存帖子列表，另一个缓存帖子总的行数。
    //帖子列表缓存 按照key缓存value
    private LoadingCache<String,List<DiscussPost>> postListCache;
    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存 从数据库中查数据 缓存到缓存中
        postListCache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
            .build(new CacheLoader<String, List<DiscussPost>>() {
                @Nullable
                @Override
                //尝试从缓存中查询数据 查询不到时 从数据库中初始化数据到缓存中
                public List<DiscussPost> load(@NonNull String key) throws Exception {
                    if(key.length() == 0){
                        throw new IllegalArgumentException("参数错误");
                    }
                    String[] params = key.split(":");
                    if(params.length != 2){
                        throw new IllegalArgumentException("参数错误");
                    }
                    int offset = Integer.parseInt(params[0]);
                    int limit = Integer.parseInt(params[1]);
                    //这里可以访问二级缓存 redis 之后再访问mysql数据库
                    logger.info("从数据库中加载帖子数据");
                    //从数据中加载帖子
                    return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                }
            });

        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
            .build(new CacheLoader<Integer, Integer>() {
                @Nullable
                @Override
                public Integer load(@NonNull Integer key) throws Exception {
                    logger.info("从数据库中查询帖子列表");
                    return discussPostMapper.selectDiscussPostRows(key);
                }
            });
    }
    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit,Integer orderMode) {
        //访问首页(不传userId 也就是userId == 1)和orderMode == 1的时候缓存
        if(userId == 0 && orderMode == 1){
            //从缓存取数据
           return postListCache.get(offset + ":" + limit);
        }
        //访问数据库
        logger.info("从数据库中加载帖子列表");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    @Override
    public int findDiscussPostRows(Integer userId) {
        if(userId == 0){
            return postRowsCache.get(userId);
        }
        //访问数据库
        logger.info("从数据库中加载帖子的个数");
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
