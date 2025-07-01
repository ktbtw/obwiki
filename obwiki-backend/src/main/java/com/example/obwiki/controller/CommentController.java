package com.example.obwiki.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public List<Comment> getCommentsByPostId(@RequestParam Long postId, @RequestParam Long userId) {
        return commentService.getCommentsByPostId(postId, userId);
    }

    @PostMapping("/vote/{id}")
    public void voteComment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Boolean cancel = body.get("cancel") != null && (Boolean) body.get("cancel");
        commentService.voteComment(id, userId, cancel);
    }
} 