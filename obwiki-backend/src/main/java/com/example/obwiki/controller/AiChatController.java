package com.example.obwiki.controller;

import com.example.obwiki.rep.AiChatReq;
import com.example.obwiki.resp.AiChatResp;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.service.IAiChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * AI对话控制器
 */
@Slf4j
@RestController
@RequestMapping("/obwiki/ai-chat")
@Api(tags = "AI对话接口")
public class AiChatController {

    private final IAiChatService aiChatService;

    public AiChatController(IAiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    @ApiOperation("发送消息给AI并获取回复")
    public CommonResp<AiChatResp> chat(@Valid @RequestBody AiChatReq req, @RequestParam Long userId) {
        log.info("用户 {} 发送AI对话请求: {}", userId, req.getMessage());
        return aiChatService.chat(req, userId);
    }

    @PostMapping("/chat/stream")
    @ApiOperation("流式发送消息给AI")
    public void chatStream(@Valid @RequestBody AiChatReq req, @RequestParam Long userId, HttpServletResponse response) {
        log.info("用户 {} 发送流式AI对话请求: {}", userId, req.getMessage());
        aiChatService.chatStream(req, userId, response);
    }

    @GetMapping("/sessions")
    @ApiOperation("获取用户的会话列表")
    public CommonResp<List<IAiChatService.AiChatSessionResp>> getSessionList(@RequestParam Long userId) {
        log.info("用户 {} 获取会话列表", userId);
        return aiChatService.getSessionList(userId);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @ApiOperation("获取会话的消息历史")
    public CommonResp<List<IAiChatService.AiChatMessageResp>> getMessageHistory(@PathVariable Long sessionId, @RequestParam Long userId) {
        log.info("用户 {} 获取会话 {} 的消息历史", userId, sessionId);
        return aiChatService.getMessageHistory(sessionId, userId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ApiOperation("删除会话")
    public CommonResp<Void> deleteSession(@PathVariable Long sessionId, @RequestParam Long userId) {
        log.info("用户 {} 删除会话 {}", userId, sessionId);
        return aiChatService.deleteSession(sessionId, userId);
    }

    @DeleteMapping("/sessions/{sessionId}/messages")
    @ApiOperation("清空会话消息")
    public CommonResp<Void> clearSession(@PathVariable Long sessionId, @RequestParam Long userId) {
        log.info("用户 {} 清空会话 {} 的消息", userId, sessionId);
        return aiChatService.clearSession(sessionId, userId);
    }
} 