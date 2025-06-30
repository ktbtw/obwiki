package com.example.obwiki.rep;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@ApiModel("电子书分页查询请求")
public class EbookQueryReq extends PageReq {
    @ApiModelProperty(value = "电子书ID", example = "1001")
    private Long id;

    @ApiModelProperty(value = "电子书名称（模糊搜索）", example = "鲸鱼")
    private String name;

    @ApiModelProperty(value = "分类2的ID", example = "2")
    private Long categoryId2;
}
