package com.dmswide.nowcoder.quartz;

import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.service.ElasticsearchService;
import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements CommunityConstant, Job {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private LikeService likeService;
    @Resource
    private ElasticsearchService elasticsearchService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //牛客纪元
    private static final Date epoch;
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("牛客纪元初始化失败",e);
        }
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations<String, Object> operations = redisTemplate.boundSetOps(redisKey);
        if(operations.size() == 0){
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }
        logger.info("[任务开始]正在刷新帖子分数" + operations.size());
        while (operations.size() > 0){
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束]帖子分数刷新完毕");
    }

    private void refresh(Integer postId){
        DiscussPost discussPost = discussPostService.findDiscussPostById(postId);
        if(discussPost == null){
            logger.error("id为:" + postId + "的帖子不存在");
            return;
        }
        //开始计算分数
        //是否加精
        boolean wonderful = discussPost.getStatus() == 1;
        //评论数量
        int commentCount = discussPost.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //计算分数
        double score = Math.log10(Math.max(w, 1)) +
            (discussPost.getCreateTime().getTime() - epoch.getTime()) * 1.0 / (1000 * 24 * 60 * 60);
        //更新帖子分数
        discussPostService.updateScore(postId,score);
        //同步搜索数据
        discussPost.setScore(score);
        elasticsearchService.saveDiscussPost(discussPost);
    }
}
