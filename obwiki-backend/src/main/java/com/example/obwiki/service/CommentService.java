package com.example.obwiki.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.obwiki.entity.Comment;
import com.example.obwiki.mapper.CommentMapper;
import com.example.obwiki.websocket.WsServiceAsync;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private WsServiceAsync wsServiceAsync;

    public int addComment(Comment comment) {
        System.out.println("插入评论参数：" + comment);
        int result = commentMapper.insert(comment);
        wsServiceAsync.sendInfo("帖子有新动态");
        return result;
    }

    public List<Comment> getCommentsByPostId(Long postId, Long currentUserId) {
        // 1. 获取该帖子的所有评论（扁平列表）
        List<Comment> allComments = commentMapper.selectByPostId(postId);
        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }
        // 设置isVoted
        for (Comment comment : allComments) {
            boolean isVoted = commentMapper.existsVote(comment.getId(), currentUserId) > 0;
            comment.setIsVoted(isVoted);
        }
        // 2. 将列表转换为Map，方便快速查找
        Map<Long, Comment> commentMap = new HashMap<>();
        for (Comment comment : allComments) {
            commentMap.put(comment.getId(), comment);
        }

        // 3. 组装树形结构
        List<Comment> rootComments = new ArrayList<>();
        for (Comment comment : allComments) {
            Long parentId = comment.getParentId();
            if (parentId == null || parentId == 0) {
                // 找到根节点
                rootComments.add(comment);
            } else {
                // 找到父节点，并把自己加到父节点的children列表中
                Comment parent = commentMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(comment);
                    // 设置被回复人的用户名，兜底处理
                    String parentName = parent.getUsername();
                    if (parentName == null || parentName.trim().isEmpty()) {
                        parentName = String.valueOf(parent.getUserId());
                    }
                    comment.setReplyToUsername(parentName);
                }
            }
        }
        // 日志排查：打印每条评论的id、userId、username
        for (Comment comment : allComments) {
            System.out.println("评论ID: " + comment.getId() + ", userId: " + comment.getUserId() + ", username: " + comment.getUsername());
        }
        return rootComments;
    }

    public void voteComment(Long commentId, Long userId, boolean cancel) {
        if (cancel) {
            commentMapper.decreaseVoteCount(commentId);
            commentMapper.deleteVote(commentId, userId);
        } else {
            if (commentMapper.existsVote(commentId, userId) == 0) {
                commentMapper.insertVote(commentId, userId);
                commentMapper.increaseVoteCount(commentId);
            }
        }
        wsServiceAsync.sendInfo("帖子有新动态");
    }

    public Comment getCommentById(Long id) {
        return commentMapper.selectById(id);
    }
} 