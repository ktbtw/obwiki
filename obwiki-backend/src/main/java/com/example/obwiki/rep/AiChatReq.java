package com.example.obwiki.rep;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * AI对话请求DTO
 */
@Data
public class AiChatReq {

    /**
     * 会话ID（可选，如果不提供则创建新会话）
     */
    private Long sessionId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 模型名称
     */
    private String model = "doubao-seed-1-6-250615";

    /**
     * 温度参数（0-2）
     */
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    private Integer maxTokens = 2048;

    /**
     * 是否流式响应
     */
    private Boolean isStream = false;
} 