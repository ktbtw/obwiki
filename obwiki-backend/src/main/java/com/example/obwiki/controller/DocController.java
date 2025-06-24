package com.example.obwiki.controller;

import com.example.obwiki.rep.DocQueryReq;
import com.example.obwiki.rep.DocSaveReq;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.resp.DocQueryResp;
import com.example.obwiki.resp.PageResp;
import com.example.obwiki.service.IDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
@RestController
@RequestMapping("/doc")
public class DocController {

    @Autowired
    private IDocService docService;

    @GetMapping("/all")
    public CommonResp all() {
        CommonResp<List<DocQueryResp>> resp = new CommonResp<>(true,"查询成功",null);

        List<DocQueryResp> list = docService.all();
        resp.setContent(list);
        return resp;
    }

    @GetMapping("/list")
    public CommonResp list(@Valid DocQueryReq req) {
        CommonResp<PageResp<DocQueryResp>> resp = new CommonResp<>(true,"查询成功",null);

        PageResp<DocQueryResp> pageResp = docService.listByname(req);
        resp.setContent(pageResp);

        return resp;
    }

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DocSaveReq req) {
        CommonResp resp = new CommonResp<>(true,"成功",null);
        docService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>(true,"删除成功",null);
        docService.delete(id);
        return resp;
    }
}
