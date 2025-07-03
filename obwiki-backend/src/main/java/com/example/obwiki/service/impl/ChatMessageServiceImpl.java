package com.example.obwiki.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.obwiki.entity.ChatMessage;
import com.example.obwiki.mapper.ChatMessageMapper;
import com.example.obwiki.service.IChatMessageService;

@Service
public class ChatMessageServiceImpl implements IChatMessageService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public void saveMessage(ChatMessage msg) {
        chatMessageMapper.insert(msg);
    }

    @Override
    public List<ChatMessage> listHistory(Long userId, Long friendId) {
        return chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChatMessage>()
                .and(qw -> qw
                    .eq("from_user_id", userId).eq("to_user_id", friendId)
                    .or()
                    .eq("from_user_id", friendId).eq("to_user_id", userId)
                )
                .orderByAsc("time"));
    }
} 