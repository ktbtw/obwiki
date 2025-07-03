package com.example.obwiki.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class Friend implements Serializable {
    private Long id;
    private Long userId;
    private Long friendId;
    private Date createTime;
} 