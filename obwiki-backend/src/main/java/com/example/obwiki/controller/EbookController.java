package com.example.obwiki.controller;

import com.example.obwiki.rep.EbookQueryReq;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.resp.EbookQueryResp;
import com.example.obwiki.service.IEbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 电子书 前端控制器
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
@RestController
@RequestMapping("/obwiki/ebook")
public class EbookController {

    @Autowired
    private IEbookService ebookService;


    @GetMapping("/list")
    public CommonResp list(@Valid EbookQueryReq req) {

        CommonResp resp = new CommonResp<>(true,"查询成功",null);
        List<EbookQueryResp> list = ebookService.listByname(req);
        resp.setContent(list);

        return resp;
    }
}
