package com.example.obwiki.controller;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.example.obwiki.req.PostSaveReq;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.resp.PostQueryResp;
import com.example.obwiki.service.PostService;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Resource
    private PostService postService;

    @GetMapping("/list")
    public CommonResp<List<PostQueryResp>> list() {
        CommonResp<List<PostQueryResp>> resp = new CommonResp<>();
        List<PostQueryResp> list = postService.list();
        resp.setContent(list);
        return resp;
    }

    @GetMapping("/{id}")
    public CommonResp<PostQueryResp> get(@PathVariable Long id) {
        CommonResp<PostQueryResp> resp = new CommonResp<>();
        PostQueryResp post = postService.get(id);
        resp.setContent(post);
        return resp;
    }

    @PostMapping
    public CommonResp<Long> save(@RequestBody PostSaveReq req) {
        CommonResp<Long> resp = new CommonResp<>();
        Long postId = postService.save(req);
        resp.setContent(postId);
        return resp;
    }

    @PostMapping("/{id}/vote")
    public CommonResp<Void> vote(@PathVariable Long id) {
        CommonResp<Void> resp = new CommonResp<>();
        postService.vote(id);
        return resp;
    }
} 