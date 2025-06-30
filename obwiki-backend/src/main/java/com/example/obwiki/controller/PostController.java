package com.example.obwiki.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.obwiki.entity.Post;
import com.example.obwiki.service.PostService;

@RestController
@RequestMapping("/obwiki/api/post")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("")
    public int addPost(@RequestBody Post post) {
        return postService.addPost(post);
    }

    @GetMapping("/list")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }
} 