package com.dmswide.nowcoder.event;

import com.alibaba.fastjson.JSONObject;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.Event;
import com.dmswide.nowcoder.entity.Message;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.service.ElasticsearchService;
import com.dmswide.nowcoder.service.MessageService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Resource
    private MessageService messageService;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Value("${wk.image.command}")
    private String wkImageCommand;
    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;
    //注入执行定时任务的线程池
    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleMultiEvents(ConsumerRecord<String,Object> record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        //系统发送站内同时
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //额外的提示信息数据 message的content字段
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        if(!event.getData().isEmpty()) {
            for(Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord<String,Object> record){
        if(record == null || record.value() == null){
            logger.error("消息内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误!");
            return;
        }

        //从数据库中查帖子 并存储到es服务器里
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    //消费删帖事件，逻辑和上面的消费发帖事件相似
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord<String,Object> record){
        if(record == null || record.value() == null){
            logger.error("消息内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误");
            return;
        }

        //从es服务器中删除帖子
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    //消费生成长图事件
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShare(ConsumerRecord<String,Object> record){
        if(record == null || record.value() == null){
            logger.error("消息内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        //获取数据
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        //拼出 cmd 命令
        String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("长图生成成功");
        } catch (IOException e) {
           logger.error("长图生成失败" + e.getMessage());
        }

        //图片生成了 将图片传到七牛云上去 但是需要注意的是：生成图片的耗时比较长所以需要定时(500ms)去查看图片是否生成了
        //启用定时器 监视图片生成 一旦申城长图就上传七牛云
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = threadPoolTaskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

     class UploadTask implements Runnable{
        private String fileName;
        private String suffix;
        //启动任务的返回值 用来停止定时器
        private Future future;

        //确保定时任务最终无论成功或者失败一定会停止
        //生成长图任务开始时间
        private long startTime;
        //重复上传到七牛云的次数
        private int uploadTimes;

        public void setFuture(Future future) {
            this.future = future;
        }

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            //定时器启动后需要停止 在线程体里面执行停止
            //长图生成时间过长 强制终止任务
            if(System.currentTimeMillis() - startTime > 30000){
                logger.error("长图生成的时间过长,强制终止任务" + fileName);
                future.cancel(true);
                return;
            }
            //生成了 但是上传失败
            if(uploadTimes >= 3){
                logger.error("上传到服务器次数过多,强制终止任务" + fileName);
                future.cancel(true);
                return;
            }
            //正常执行 找到长图文件上传到服务器
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s]",++uploadTimes,fileName));
                //正式上传文件
                //设置相应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                //生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 60 * 60, policy);
                //指定上传的机房
                UploadManager uploadManager = new UploadManager(new Configuration(Zone.zone0()));
                try{
                    //上传图片
                    try{
                        //图片太大 导致图片还没有完全生成 就被传到七牛云上 长图大小为0kb 所以让上传的线程sleep(1000)等待图片完全生成 再上传
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Response response = uploadManager.put(path, fileName, uploadToken, null, "image/" + suffix, false);

                    //处理响应结果 从返回的结果生成json对象
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if(json == null || json.get("code") == null || !("0").equals(String.valueOf(json.get("code")))){
                        logger.error(String.format("第%d次上传文件[%s]失败",uploadTimes,fileName));
                    }else {
                        logger.error(String.format("第%d次上传文件[%s]成功",uploadTimes,fileName));
                        //上传成功 定时任务需要强制结束
                        future.cancel(true);
                    }
                }catch (QiniuException exception){
                    logger.error(String.format("第%d次上传文件[%s]失败",uploadTimes,fileName));
                }
            }else{
                logger.info("长图还未生成,等待[" + fileName + "]的生成");
            }
        }
    }
}
