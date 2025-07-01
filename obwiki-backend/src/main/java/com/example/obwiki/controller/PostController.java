package com.example.obwiki.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.obwiki.dto.PostDetailDto;
import com.example.obwiki.entity.Post;
import com.example.obwiki.service.PostService;

@RestController
@RequestMapping("/obwiki/api/post")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("/save")
    public int addPost(@RequestBody Post post) {
        return postService.addPost(post);
    }

    @GetMapping("/list")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public PostDetailDto getPostById(@PathVariable Long id, @RequestParam Long userId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return postService.getPostById(id, userId, ip);
    }

    @PostMapping("/vote/{id}")
    public void votePost(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Boolean cancel = body.get("cancel") != null && (Boolean) body.get("cancel");
        postService.votePost(id, userId, cancel);
    }
} 