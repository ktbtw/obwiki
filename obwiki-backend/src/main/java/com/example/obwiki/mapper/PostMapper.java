package com.example.obwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.obwiki.entity.Post;

@Mapper
public interface PostMapper {
    int insert(Post post);
    List<Post> selectAll();
    Post selectById(@Param("id") Long id);
    void increaseVoteCount(@Param("id") Long id);
    void increaseViewCount(@Param("id") Long id);
    int insertVote(@Param("postId") Long postId, @Param("userId") Long userId);
    int deleteVote(@Param("postId") Long postId, @Param("userId") Long userId);
    int existsVote(@Param("postId") Long postId, @Param("userId") Long userId);
    int decreaseVoteCount(@Param("id") Long id);
    int countViewLogByUser(@Param("postId") Long postId, @Param("userId") Long userId);
    int countViewLogByIp(@Param("postId") Long postId, @Param("ip") String ip);
    int insertViewLog(@Param("postId") Long postId, @Param("userId") Long userId, @Param("ip") String ip);
} 