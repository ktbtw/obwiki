package com.example.obwiki.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.obwiki.entity.Post;
import com.example.obwiki.mapper.PostMapper;

@Service
public class PostService {
    @Autowired
    private PostMapper postMapper;

    public int addPost(Post post) {
        return postMapper.insert(post);
    }

    public List<Post> getAllPosts() {
        return postMapper.selectAll();
    }

    public Post getPostById(Long id) {
        return postMapper.selectById(id);
    }
} 