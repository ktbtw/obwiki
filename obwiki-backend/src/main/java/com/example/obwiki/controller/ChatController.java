package com.example.obwiki.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.obwiki.entity.ChatMessage;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.service.IChatMessageService;

@RestController
@RequestMapping("/obwiki/chat")
public class ChatController {
    @Autowired
    private IChatMessageService chatMessageService;

    @GetMapping("/history")
    public CommonResp<List<ChatMessage>> history(@RequestParam Long userId, @RequestParam Long friendId) {
        List<ChatMessage> list = chatMessageService.listHistory(userId, friendId);
        CommonResp<List<ChatMessage>> resp = new CommonResp<>();
        resp.setContent(list);
        return resp;
    }
} 