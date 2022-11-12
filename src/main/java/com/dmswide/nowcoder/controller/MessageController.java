package com.dmswide.nowcoder.controller;

import com.alibaba.fastjson.JSONObject;
import com.dmswide.nowcoder.entity.Message;
import com.dmswide.nowcoder.entity.Page;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.MessageService;
import com.dmswide.nowcoder.service.UserService;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import org.apache.ibatis.annotations.Param;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Resource
    private MessageService messageService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserService userService;

    @GetMapping("/letter/list")
    public String getLetter(Model model, Page page){
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationsCount(user.getId()));
        //会话列表

        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        //遍历conversationList来构造conversations
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            conversationList.forEach(message -> {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLettersCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLettersUnreadCount(user.getId(),message.getConversationId()));
                Integer targetId = user.getId().equals(message.getFromId()) ? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            });
        }
        model.addAttribute("conversations",conversations);

        //查询未读私信的数量
        int lettersUnreadCount = messageService.findLettersUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",lettersUnreadCount);
        //系统消息未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Model model,Page page){
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLettersCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList != null){
            letterList.forEach(letter -> {
                Map<String,Object> map = new HashMap<>();
                map.put("letter",letter);
                map.put("fromUser",userService.findUserById(letter.getFromId()));
                letters.add(map);
            });
        }

        model.addAttribute("letters",letters);

        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));
        //设置为已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            System.out.println("修改为已读正在执行");
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            letterList.forEach(message -> {
                //是消息得接收者 而且消息是未读得状态
                if(hostHolder.getUser().getId().equals(message.getToId()) && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            });
        }
        return ids;
    }
    /**
     *
     * @param toName 接收者的用户名
     * @param content 私信内容
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName,String content){
        //得对话目标
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());

        if(hostHolder.getUser().getId() < target.getId()){
            message.setConversationId(hostHolder.getUser().getId() + "_"+ target.getId());
        }else{
            message.setConversationId(target.getId() + "_"+ hostHolder.getUser().getId());
        }

        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if(message != null){
            Map<String,Object> messageVO = new HashMap<>();

            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data  = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread",unread);

            model.addAttribute("commentNotice",messageVO);
        }


        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if(message != null){
            Map<String,Object> messageVO = new HashMap<>();

            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data  = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread",unread);

            model.addAttribute("likeNotice",messageVO);
        }


        //查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if(message != null){
            Map<String,Object> messageVO = new HashMap<>();

            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data  = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread",unread);

            model.addAttribute("followNotice",messageVO);
        }


        // TODO: 2022/11/3 dmsWide 查询未读消息数量:私信 和 系统通知
        int lettersUnreadCount = messageService.findLettersUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",lettersUnreadCount);

        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic,Page page,Model model){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(noticeList != null){
            noticeList.forEach(notice -> {
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));

                //系统消息通知的作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            });
        }
        model.addAttribute("notices",noticeVoList);

        //设置系统消息已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
