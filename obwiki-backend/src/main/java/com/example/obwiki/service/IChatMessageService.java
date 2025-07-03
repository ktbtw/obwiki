package com.example.obwiki.service;

import java.util.List;
import com.example.obwiki.entity.ChatMessage;

public interface IChatMessageService {
    void saveMessage(ChatMessage msg);
    List<ChatMessage> listHistory(Long userId, Long friendId);
} 