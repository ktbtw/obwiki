package com.example.obwiki.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.obwiki.entity.Comment;
import com.example.obwiki.service.CommentService;

@RestController
@RequestMapping("/obwiki/api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("")
    public int addComment(@RequestBody Comment comment) {
        return commentService.addComment(comment);
    }

    @GetMapping("/list")
    public List<Comment> getCommentsByPostId(@RequestParam Long postId) {
        return commentService.getCommentsByPostId(postId);
    }
} 