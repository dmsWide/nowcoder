package com.dmswide.nowcoder.event;

import com.alibaba.fastjson.JSONObject;
import com.dmswide.nowcoder.entity.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class EventProducer {
    @Resource
    private KafkaTemplate<String,Object> kafkaTemplate;

    //处理事件 本质上是发送消息
    public void fireEvent(Event event){
        //将事件发送给指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
