package com.example.obwiki.rep;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CategoryQueryReq extends PageReq  {
    //名称
    private String name;
}