package com.example.obwiki.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.obwiki.entity.Comment;
import com.example.obwiki.mapper.CommentMapper;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    public int addComment(Comment comment) {
        return commentMapper.insert(comment);
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentMapper.selectByPostId(postId);
    }

    public void voteComment(Long id) {
        commentMapper.increaseVoteCount(id);
    }

    public Comment getCommentById(Long id) {
        return commentMapper.selectById(id);
    }
} 