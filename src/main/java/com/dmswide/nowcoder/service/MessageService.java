package com.dmswide.nowcoder.service;

import com.dmswide.nowcoder.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageService {
    // TODO: 2022/10/28 dmsWide
    List<Message> findConversations(Integer userId,Integer offset,Integer limit);

    // TODO: 2022/10/28 dmsWide
    int findConversationsCount(Integer userId);

    // TODO: 2022/10/28 dmsWide
    List<Message> findLetters(String conversationId,Integer offset,Integer limit);

    // TODO: 2022/10/28 dmsWide
    int findLettersCount(String conversationId);

    // TODO: 2022/10/28 dmsWide
    int findLettersUnreadCount(Integer userId,String conversationId);

    // TODO: 2022/10/28 dmsWide 添加一条消息
    int addMessage(Message message);

    // TODO: 2022/10/28 dmsWide 读取消息 消息设置为已读
    int readMessage(List<Integer> ids);

    // TODO: 2022/11/3 dmsWide 查询某个主题下最新的通知
    Message findLatestNotice(Integer userId,String topic);

    // TODO: 2022/11/3 dmsWide 查询某个主题下包含的通知的数量
    int findNoticeCount(Integer userId,String topic);

    // TODO: 2022/11/3 dmsWide 查询未读的通知的数量
    int findNoticeUnreadCount(Integer userId,String topic);

    // TODO: 2022/11/3 dmsWide 查询全部的消息
    List<Message> findNotices(Integer userId,String topic,Integer offset,Integer limit);
}
