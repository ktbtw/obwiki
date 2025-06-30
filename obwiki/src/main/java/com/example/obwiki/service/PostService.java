package com.example.obwiki.service;

import java.util.List;
import com.example.obwiki.req.PostSaveReq;
import com.example.obwiki.resp.PostQueryResp;

public interface PostService {
    List<PostQueryResp> list();
    PostQueryResp get(Long id);
    Long save(PostSaveReq req);
    void vote(Long id);
} 