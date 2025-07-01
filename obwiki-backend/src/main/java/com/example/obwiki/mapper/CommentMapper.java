package com.example.obwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.obwiki.entity.Comment;

@Mapper
public interface CommentMapper {
    int insert(Comment comment);
    List<Comment> selectByPostId(@Param("postId") Long postId);
    void increaseVoteCount(@Param("id") Long id);
    Comment selectById(@Param("id") Long id);
    int insertVote(@Param("commentId") Long commentId, @Param("userId") Long userId);
    int deleteVote(@Param("commentId") Long commentId, @Param("userId") Long userId);
    int existsVote(@Param("commentId") Long commentId, @Param("userId") Long userId);
    int decreaseVoteCount(@Param("id") Long id);
} 