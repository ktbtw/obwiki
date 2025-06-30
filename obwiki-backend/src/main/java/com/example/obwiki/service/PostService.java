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

    public PostDetailDto getPostById(Long id) {
        postMapper.increaseViewCount(id);
        Post post = postMapper.selectById(id);
        List<Comment> comments = commentService.getCommentsByPostId(id);

        PostDetailDto postDetailDto = new PostDetailDto();
        BeanUtils.copyProperties(post, postDetailDto);
        postDetailDto.setComments(comments);

        return postDetailDto;
    }

    public void votePost(Long id) {
        postMapper.increaseVoteCount(id);
    }
} 