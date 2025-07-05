package com.example.obwiki.resp;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * AI对话响应DTO
 */
@Data
public class AiChatResp {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * AI回复内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 消息历史（可选）
     */
    private List<MessageHistory> messageHistory;

    @Data
    public static class MessageHistory {
        private String role;
        private String content;
        private Date createTime;
    }
} 