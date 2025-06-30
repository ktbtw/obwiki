package com.example.obwiki.service.impl;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.obwiki.mapper.PostMapper;
import com.example.obwiki.req.PostSaveReq;
import com.example.obwiki.resp.PostQueryResp;
import com.example.obwiki.util.CopyUtil;

@Service
public class PostServiceImpl implements PostService {

    @Resource
    private PostMapper postMapper;

    @Override
    public List<PostQueryResp> list() {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        List<Post> postList = postMapper.selectList(wrapper);
        return CopyUtil.copyList(postList, PostQueryResp.class);
    }

    @Override
    public PostQueryResp get(Long id) {
        Post post = postMapper.selectById(id);
        // 更新阅读数
        if (post != null) {
            Post updatePost = new Post();
            updatePost.setId(id);
            updatePost.setViewCount(post.getViewCount() + 1);
            postMapper.updateById(updatePost);
            post.setViewCount(post.getViewCount() + 1);
        }
        return CopyUtil.copy(post, PostQueryResp.class);
    }

    @Override
    public Long save(PostSaveReq req) {
        Post post = CopyUtil.copy(req, Post.class);
        if (post.getId() == null) {
            post.setCreateTime(new Date());
            post.setViewCount(0);
            post.setVoteCount(0);
            postMapper.insert(post);
        } else {
            postMapper.updateById(post);
        }
        return post.getId();
    }

    @Override
    public void vote(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null) {
            Post updatePost = new Post();
            updatePost.setId(id);
            updatePost.setVoteCount(post.getVoteCount() + 1);
            postMapper.updateById(updatePost);
        }
    }
} 