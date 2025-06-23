package com.example.obwiki.controller;

import com.example.obwiki.entity.Ebook;
import com.example.obwiki.rep.EbookQueryReq;
import com.example.obwiki.rep.EbookSaveReq;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.resp.EbookQueryResp;
import com.example.obwiki.resp.PageResp;
import com.example.obwiki.service.IEbookService;
import com.example.obwiki.utils.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

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
        CommonResp<PageResp<EbookQueryResp>> resp = new CommonResp<>(true,"查询成功",null);

        PageResp<EbookQueryResp> pageResp = ebookService.listByname(req);
        resp.setContent(pageResp);

        return resp;
    }

    @GetMapping("/all")
    public CommonResp<List<EbookQueryResp>> listAll() {
        List<Ebook> ebookList = ebookService.list();
        List<EbookQueryResp> resps = CopyUtil.copyList(ebookList, EbookQueryResp.class);
        return new CommonResp<>(true, "查询全部成功", resps);
    }

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody EbookSaveReq req) {
        CommonResp resp = new CommonResp<>(true,"成功",null);
        ebookService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>();
        ebookService.delete(id);
        return resp;
    }
}
