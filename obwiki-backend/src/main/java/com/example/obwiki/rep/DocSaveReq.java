package com.example.obwiki.rep;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DocSaveReq {
    private Long id;

    @NotNull(message = "【电子书】不能为空")
    private Long ebookId;

    @NotNull(message = "【父文档】不能为空")
    private Long parent;

    @NotNull(message = "【名称】不能为空")
    private String name;

    private Integer sort;

    private Integer viewCount;

    private Integer voteCount;

    private String content;

    private Double lat;
    private Double lng;
}