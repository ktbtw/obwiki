package com.example.obwiki.dto;

import java.util.List;
import com.example.obwiki.entity.Comment;
import com.example.obwiki.entity.Post;

public class PostDetailDto extends Post {
    private List<Comment> comments;
    private String avatar;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
} 