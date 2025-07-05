package com.example.obwiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.obwiki.entity.AiChatMessage;
import com.example.obwiki.entity.AiChatSession;
import com.example.obwiki.mapper.AiChatMessageMapper;
import com.example.obwiki.mapper.AiChatSessionMapper;
import com.example.obwiki.rep.AiChatReq;
import com.example.obwiki.resp.AiChatResp;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.service.IAiChatService;
import com.example.obwiki.utils.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI对话服务实现类
 */
@Slf4j
@Service
public class AiChatServiceImpl implements IAiChatService {

    @Value("${ark.api.key:e55fbc84-307f-4f51-9dce-8bfc14266b4c}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final SnowFlake snowFlake;

    public AiChatServiceImpl(AiChatSessionMapper sessionMapper, 
                           AiChatMessageMapper messageMapper, 
                           SnowFlake snowFlake) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.snowFlake = snowFlake;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public CommonResp<AiChatResp> chat(AiChatReq req, Long userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 获取或创建会话
            Long sessionId = getOrCreateSession(req.getSessionId(), userId, req.getMessage());
            
            // 2. 保存用户消息
            saveUserMessage(sessionId, userId, req);
            
            // 3. 构建对话历史
            List<Map<String, String>> chatMessages = buildChatHistory(sessionId);
            
            // 4. 调用AI API
            String aiContent = callAiApi(req, chatMessages);
            
            // 5. 保存AI回复
            saveAiMessage(sessionId, userId, aiContent, req);
            
            // 6. 更新会话信息
            updateSession(sessionId, req.getMessage());
            
            // 7. 构建响应
            AiChatResp resp = new AiChatResp();
            resp.setSessionId(sessionId);
            resp.setContent(aiContent);
            resp.setModel(req.getModel());
            resp.setResponseTime(System.currentTimeMillis() - startTime);
            resp.setCreateTime(new Date());
            
            return new CommonResp<>(true, "AI对话成功", resp);
            
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return new CommonResp<>(false, "AI对话失败: " + e.getMessage(), null);
        }
    }

    @Override
    public void chatStream(AiChatReq req, Long userId, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 设置响应头
            response.setContentType("text/plain;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "Cache-Control");
            
            PrintWriter writer = response.getWriter();
            
            // 1. 获取或创建会话
            Long sessionId = getOrCreateSession(req.getSessionId(), userId, req.getMessage());
            
            // 2. 保存用户消息
            saveUserMessage(sessionId, userId, req);
            
            // 3. 构建对话历史
            List<Map<String, String>> chatMessages = buildChatHistory(sessionId);
            
            // 4. 调用AI API（流式）
            String aiContent = callAiApiStream(req, chatMessages, writer);
            
            // 5. 保存AI回复
            saveAiMessage(sessionId, userId, aiContent, req);
            
            // 6. 更新会话信息
            updateSession(sessionId, req.getMessage());
            
        } catch (Exception e) {
            log.error("流式AI对话失败", e);
            try {
                PrintWriter writer = response.getWriter();
                writer.write("data: [ERROR] " + e.getMessage() + "\n\n");
                writer.flush();
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    @Override
    public CommonResp<List<AiChatSessionResp>> getSessionList(Long userId) {
        try {
            QueryWrapper<AiChatSession> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("is_deleted", false)
                   .orderByDesc("update_time");
            
            List<AiChatSession> sessions = sessionMapper.selectList(wrapper);
            
            List<AiChatSessionResp> respList = sessions.stream()
                    .map(session -> {
                        AiChatSessionResp resp = new AiChatSessionResp();
                        resp.setId(session.getId());
                        resp.setTitle(session.getTitle());
                        resp.setCreateTime(session.getCreateTime());
                        resp.setUpdateTime(session.getUpdateTime());
                        resp.setMessageCount(session.getMessageCount());
                        return resp;
                    })
                    .collect(Collectors.toList());
            
            return new CommonResp<>(true, "获取会话列表成功", respList);
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            return new CommonResp<>(false, "获取会话列表失败: " + e.getMessage(), null);
        }
    }

    @Override
    public CommonResp<List<AiChatMessageResp>> getMessageHistory(Long sessionId, Long userId) {
        try {
            // 验证会话归属
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                return new CommonResp<>(false, "会话不存在或无权限访问", null);
            }
            
            QueryWrapper<AiChatMessage> wrapper = new QueryWrapper<>();
            wrapper.eq("session_id", sessionId)
                   .orderByAsc("create_time");
            
            List<AiChatMessage> messages = messageMapper.selectList(wrapper);
            
            List<AiChatMessageResp> respList = messages.stream()
                    .map(message -> {
                        AiChatMessageResp resp = new AiChatMessageResp();
                        resp.setId(message.getId());
                        resp.setRole(message.getRole());
                        resp.setContent(message.getContent());
                        resp.setCreateTime(message.getCreateTime());
                        return resp;
                    })
                    .collect(Collectors.toList());
            
            return new CommonResp<>(true, "获取消息历史成功", respList);
        } catch (Exception e) {
            log.error("获取消息历史失败", e);
            return new CommonResp<>(false, "获取消息历史失败: " + e.getMessage(), null);
        }
    }

    @Override
    public CommonResp<Void> deleteSession(Long sessionId, Long userId) {
        try {
            // 验证会话归属
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                return new CommonResp<>(false, "会话不存在或无权限删除", null);
            }
            
            // 软删除会话
            UpdateWrapper<AiChatSession> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", sessionId)
                   .set("is_deleted", true);
            sessionMapper.update(null, wrapper);
            
            return new CommonResp<>(true, "删除会话成功", null);
        } catch (Exception e) {
            log.error("删除会话失败", e);
            return new CommonResp<>(false, "删除会话失败: " + e.getMessage(), null);
        }
    }

    @Override
    public CommonResp<Void> clearSession(Long sessionId, Long userId) {
        try {
            // 验证会话归属
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                return new CommonResp<>(false, "会话不存在或无权限清空", null);
            }
            
            // 删除会话下的所有消息
            QueryWrapper<AiChatMessage> wrapper = new QueryWrapper<>();
            wrapper.eq("session_id", sessionId);
            messageMapper.delete(wrapper);
            
            // 更新会话消息数量
            UpdateWrapper<AiChatSession> sessionWrapper = new UpdateWrapper<>();
            sessionWrapper.eq("id", sessionId)
                         .set("message_count", 0);
            sessionMapper.update(null, sessionWrapper);
            
            return new CommonResp<>(true, "清空会话成功", null);
        } catch (Exception e) {
            log.error("清空会话失败", e);
            return new CommonResp<>(false, "清空会话失败: " + e.getMessage(), null);
        }
    }

    /**
     * 调用AI API
     */
    private String callAiApi(AiChatReq req, List<Map<String, String>> messages) {
        try {
            String url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", req.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", req.getTemperature());
            requestBody.put("max_tokens", req.getMaxTokens());
            requestBody.put("stream", false);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return (String) message.get("content");
                }
            }
            
            throw new RuntimeException("AI API调用失败");
            
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            throw new RuntimeException("调用AI API失败: " + e.getMessage());
        }
    }

    /**
     * 调用AI API（流式）
     */
    private String callAiApiStream(AiChatReq req, List<Map<String, String>> messages, PrintWriter writer) {
        try {
            String url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", req.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", req.getTemperature());
            requestBody.put("max_tokens", req.getMaxTokens());
            requestBody.put("stream", true);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 这里简化处理，实际应该使用WebClient或SSE处理流式响应
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            // 模拟流式响应
            String content = "这是一个模拟的AI回复，实际应该处理流式响应。";
            writer.write("data: " + content + "\n\n");
            writer.flush();
            
            return content;
            
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            throw new RuntimeException("调用AI API失败: " + e.getMessage());
        }
    }

    /**
     * 获取或创建会话
     */
    private Long getOrCreateSession(Long sessionId, Long userId, String firstMessage) {
        if (sessionId != null) {
            // 验证会话是否存在且属于当前用户
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session != null && session.getUserId().equals(userId)) {
                return sessionId;
            }
        }
        
        // 创建新会话
        AiChatSession newSession = new AiChatSession();
        newSession.setId(snowFlake.nextId());
        newSession.setUserId(userId);
        newSession.setTitle(generateSessionTitle(firstMessage));
        newSession.setCreateTime(new Date());
        newSession.setUpdateTime(new Date());
        newSession.setMessageCount(0);
        newSession.setIsDeleted(false);
        
        sessionMapper.insert(newSession);
        return newSession.getId();
    }

    /**
     * 生成会话标题
     */
    private String generateSessionTitle(String message) {
        if (message.length() <= 20) {
            return message;
        }
        return message.substring(0, 20) + "...";
    }

    /**
     * 保存用户消息
     */
    private void saveUserMessage(Long sessionId, Long userId, AiChatReq req) {
        AiChatMessage message = new AiChatMessage();
        message.setId(snowFlake.nextId());
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole("user");
        message.setContent(req.getMessage());
        message.setCreateTime(new Date());
        message.setModel(req.getModel());
        message.setTemperature(req.getTemperature());
        message.setMaxTokens(req.getMaxTokens());
        message.setIsStream(req.getIsStream());
        
        messageMapper.insert(message);
    }

    /**
     * 保存AI消息
     */
    private void saveAiMessage(Long sessionId, Long userId, String content, AiChatReq req) {
        AiChatMessage message = new AiChatMessage();
        message.setId(snowFlake.nextId());
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole("assistant");
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setModel(req.getModel());
        message.setTemperature(req.getTemperature());
        message.setMaxTokens(req.getMaxTokens());
        message.setIsStream(req.getIsStream());
        
        messageMapper.insert(message);
    }

    /**
     * 构建对话历史
     */
    private List<Map<String, String>> buildChatHistory(Long sessionId) {
        QueryWrapper<AiChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
               .orderByAsc("create_time")
               .last("LIMIT 20"); // 限制历史消息数量
        
        List<AiChatMessage> messages = messageMapper.selectList(wrapper);
        
        return messages.stream()
                .map(msg -> {
                    Map<String, String> message = new HashMap<>();
                    message.put("role", msg.getRole());
                    message.put("content", msg.getContent());
                    return message;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新会话信息
     */
    private void updateSession(Long sessionId, String lastMessage) {
        // 更新会话标题（如果会话是新创建的）
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session != null && StringUtils.isEmpty(session.getTitle())) {
            UpdateWrapper<AiChatSession> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", sessionId)
                   .set("title", generateSessionTitle(lastMessage));
            sessionMapper.update(null, wrapper);
        }
        
        // 更新会话时间和消息数量
        QueryWrapper<AiChatMessage> messageWrapper = new QueryWrapper<>();
        messageWrapper.eq("session_id", sessionId);
        Long messageCount = messageMapper.selectCount(messageWrapper);
        
        UpdateWrapper<AiChatSession> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", sessionId)
                     .set("update_time", new Date())
                     .set("message_count", messageCount);
        sessionMapper.update(null, updateWrapper);
    }
} 