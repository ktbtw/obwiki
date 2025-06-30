package com.example.obwiki.req;

import lombok.Data;

@Data
public class PostSaveReq {
    private Long id;
    private Long userId;
    private String title;
    private String content;
} 