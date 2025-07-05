package com.example.obwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.obwiki.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话消息Mapper接口
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
} 