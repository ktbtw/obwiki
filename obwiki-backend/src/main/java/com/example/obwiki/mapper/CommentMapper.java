package com.example.obwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.obwiki.entity.Comment;

@Mapper
public interface CommentMapper {
    int insert(Comment comment);
    List<Comment> selectByPostId(@Param("postId") Long postId);
} 