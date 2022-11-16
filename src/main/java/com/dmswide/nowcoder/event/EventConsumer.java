package com.dmswide.nowcoder.event;

import com.alibaba.fastjson.JSONObject;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.entity.Event;
import com.dmswide.nowcoder.entity.Message;
import com.dmswide.nowcoder.service.DiscussPostService;
import com.dmswide.nowcoder.service.ElasticsearchService;
import com.dmswide.nowcoder.service.MessageService;
import com.dmswide.nowcoder.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    }
}
