package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.MessageMapper;
import com.dmswide.nowcoder.entity.Message;
import com.dmswide.nowcoder.service.MessageService;
import com.dmswide.nowcoder.util.SensitiveWordsFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private SensitiveWordsFilter sensitiveWordsFilter;
    @Override
    public List<Message> findConversations(Integer userId, Integer offset, Integer limit) {
        return messageMapper.selectConversations(userId,offset,limit);
    }

    @Override
    public int findConversationsCount(Integer userId) {
        return messageMapper.selectConversationsCount(userId);
    }

    @Override
    public List<Message> findLetters(String conversationId, Integer offset, Integer limit) {
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    @Override
    public int findLettersCount(String conversationId) {
        return messageMapper.selectLettersCount(conversationId);
    }

    @Override
    public int findLettersUnreadCount(Integer userId, String conversationId) {
        return messageMapper.selectLettersUnreadCount(userId,conversationId);
    }

    @Override
    public int addMessage(Message message) {
        //先过滤
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveWordsFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    @Override
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids,1);
    }

    @Override
    public Message findLatestNotice(Integer userId, String topic) {
        return messageMapper.selectLatestNotice(userId,topic);
    }

    @Override
    public int findNoticeCount(Integer userId, String topic) {
        return messageMapper.selectNoticeCount(userId,topic);
    }

    @Override
    public int findNoticeUnreadCount(Integer userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    @Override
    public List<Message> findNotices(Integer userId, String topic, Integer offset, Integer limit) {
        return messageMapper.selectNotice(userId,topic,offset,limit);
    }
}
