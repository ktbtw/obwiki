package com.example.obwiki.service;

import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.obwiki.dto.PostDetailDto;
import com.example.obwiki.entity.Comment;
import com.example.obwiki.entity.Post;
import com.example.obwiki.mapper.PostMapper;

@Service
public class PostService {
    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentService commentService;

    public int addPost(Post post) {
        return postMapper.insert(post);
    }

    public List<Post> getAllPosts() {
        return postMapper.selectAll();
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
    }
} 