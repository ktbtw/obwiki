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
} 