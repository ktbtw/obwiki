package com.example.obwiki.entity;

import java.util.Date;
import java.util.List;

public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Date createTime;
    private Integer voteCount;
    private Long parentId;
    private List<Comment> children;

    private String username;
    private String replyToUsername;

    private boolean isVoted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public List<Comment> getChildren() { return children; }
    public void setChildren(List<Comment> children) { this.children = children; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getReplyToUsername() { return replyToUsername; }
    public void setReplyToUsername(String replyToUsername) { this.replyToUsername = replyToUsername; }

    public boolean getIsVoted() { return isVoted; }
    public void setIsVoted(boolean isVoted) { this.isVoted = isVoted; }
} 