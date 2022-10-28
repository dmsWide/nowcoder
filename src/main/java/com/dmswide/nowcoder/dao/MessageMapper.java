package com.dmswide.nowcoder.dao;

import com.dmswide.nowcoder.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户会话列表 针对每个会话只返回最新的一条私信
    List<Message> selectConversations(@Param("userId") Integer userId,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    //当前用户的会话数量
    int selectConversationsCount(@Param("userId")Integer userId);

    //某个会话所包含的私信列表
    List<Message> selectLetters(@Param("conversationId") String conversationId,
                                @Param("offset") Integer offset,
                                @Param("limit")Integer limit);

    //某个会话所包含的私信数量
    int selectLettersCount(@Param("conversationId") String conversationId);

    /**
     *
     * @param userId 用户id 必有
     * @param conversationId 会话id 可选
     * @return
     */
    //查询未读私信数量
    int selectLettersUnreadCount(@Param("userId") Integer userId,
                                 @Param("conversationId") String conversationId);

    //新增消息
    int insertMessage(Message message);
    //修改消息的状态 已读或者删除

    //这里使用了注解@Param给参数起了别名 mapper.xml文件中使用参数ids循环变量 也可以使用list作为循环变量
    int updateStatus(@Param("ids") List<Integer> ids,@Param("status") Integer status);
}
