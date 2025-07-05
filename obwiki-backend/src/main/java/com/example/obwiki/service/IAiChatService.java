package com.example.obwiki.service;

import com.example.obwiki.rep.AiChatReq;
import com.example.obwiki.resp.AiChatResp;
import com.example.obwiki.resp.CommonResp;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * AI对话服务接口
 */
public interface IAiChatService {

    /**
     * 发送消息给AI并获取回复
     */
    CommonResp<AiChatResp> chat(AiChatReq req, Long userId);

    /**
     * 流式发送消息给AI
     */
    void chatStream(AiChatReq req, Long userId, HttpServletResponse response);

    /**
     * 获取用户的会话列表
     */
    CommonResp<List<AiChatSessionResp>> getSessionList(Long userId);

    /**
     * 获取会话的消息历史
     */
    CommonResp<List<AiChatMessageResp>> getMessageHistory(Long sessionId, Long userId);

    /**
     * 删除会话
     */
    CommonResp<Void> deleteSession(Long sessionId, Long userId);

    /**
     * 清空会话消息
     */
    CommonResp<Void> clearSession(Long sessionId, Long userId);

    /**
     * 会话响应DTO
     */
    class AiChatSessionResp {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String title;
        private Date createTime;
        private Date updateTime;
        private Integer messageCount;

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
        public Date getUpdateTime() { return updateTime; }
        public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
        public Integer getMessageCount() { return messageCount; }
        public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    }

    /**
     * 消息响应DTO
     */
    class AiChatMessageResp {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String role;
        private String content;
        private Date createTime;

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }
} 