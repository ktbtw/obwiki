package com.example.obwiki.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // 1. 获取该帖子的所有评论（扁平列表）
        List<Comment> allComments = commentMapper.selectByPostId(postId);
        if (allComments.isEmpty()) {
            return new ArrayList<>();
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
                }
            }
        }
        return rootComments;
    }

    public void voteComment(Long id) {
        commentMapper.increaseVoteCount(id);
    }

    public Comment getCommentById(Long id) {
        return commentMapper.selectById(id);
    }
} 