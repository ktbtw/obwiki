package com.example.obwiki.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.obwiki.entity.ChatMessage;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
} 