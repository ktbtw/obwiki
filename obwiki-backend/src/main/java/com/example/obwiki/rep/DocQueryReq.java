package com.example.obwiki.rep;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DocQueryReq  extends PageReq{
    //名称
    private String name;
}

