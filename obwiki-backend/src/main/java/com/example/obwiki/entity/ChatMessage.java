package com.example.obwiki.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class ChatMessage implements Serializable {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private Date time;
}