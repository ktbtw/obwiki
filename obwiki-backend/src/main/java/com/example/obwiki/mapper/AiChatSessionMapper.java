package com.example.obwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.obwiki.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话会话Mapper接口
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {
} 