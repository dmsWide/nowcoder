package com.dmswide.nowcoder.service;

import com.dmswide.nowcoder.entity.Message;

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
}
