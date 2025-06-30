package com.example.obwiki.resp;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CategoryQueryResp {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    //父id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parent;
    //名称
    private String name;
    //顺序
    private Integer sort;
}