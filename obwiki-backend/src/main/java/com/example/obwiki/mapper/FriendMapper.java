package com.example.obwiki.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.obwiki.entity.Friend;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
} 