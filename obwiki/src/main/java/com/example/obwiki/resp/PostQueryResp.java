package com.example.obwiki.resp;

import java.util.Date;
import lombok.Data;

@Data
public class PostQueryResp {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Date createTime;
    private Integer viewCount;
    private Integer voteCount;
} 