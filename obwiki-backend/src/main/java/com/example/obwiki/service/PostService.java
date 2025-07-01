package com.example.obwiki.service;

import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.obwiki.dto.PostDetailDto;
import com.example.obwiki.entity.Comment;
import com.example.obwiki.entity.Post;
import com.example.obwiki.entity.User;
import com.example.obwiki.mapper.PostMapper;
import com.example.obwiki.mapper.UserMapper;
import com.example.obwiki.websocket.WsServiceAsync;

@Service
public class PostService {
    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WsServiceAsync wsServiceAsync;

    public int addPost(Post post) {
        int result = postMapper.insert(post);
        wsServiceAsync.sendInfo("帖子有新动态");
        return result;
    }

    public List<Post> getAllPosts() {
        List<Post> posts = postMapper.selectAll();
        for (Post post : posts) {
            if (post.getUserId() != null) {
                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    post.setAvatar(user.getAvatar());
                }
            }
        }
        return posts;
    }

    public PostDetailDto getPostById(Long id, Long userId, String ip) {
        // 唯一用户唯一计数浏览量
        if (userId != null && userId != 0) {
            if (postMapper.countViewLogByUser(id, userId) == 0) {
                postMapper.insertViewLog(id, userId, null);
                postMapper.increaseViewCount(id);
            }
        } else if (ip != null && !ip.isEmpty()) {
            if (postMapper.countViewLogByIp(id, ip) == 0) {
                postMapper.insertViewLog(id, null, ip);
                postMapper.increaseViewCount(id);
            }
        }
        Post post = postMapper.selectById(id);
        List<Comment> comments = commentService.getCommentsByPostId(id, userId);

        PostDetailDto postDetailDto = new PostDetailDto();
        BeanUtils.copyProperties(post, postDetailDto);
        postDetailDto.setComments(comments);
        // 补全avatar
        if (post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                postDetailDto.setAvatar(user.getAvatar());
            }
        }
        return postDetailDto;
    }

    public void votePost(Long postId, Long userId, boolean cancel) {
        if (cancel) {
            postMapper.decreaseVoteCount(postId);
            postMapper.deleteVote(postId, userId);
        } else {
            // 防止重复点赞
            if (postMapper.existsVote(postId, userId) == 0) {
                postMapper.insertVote(postId, userId);
                postMapper.increaseVoteCount(postId);
            }
        }
        wsServiceAsync.sendInfo("帖子有新动态");
    }

    public void deletePost(Long id) {
        System.out.println("删除帖子，id=" + id);
        postMapper.deleteById(id);
        wsServiceAsync.sendInfo("帖子有新动态");
    }
} 