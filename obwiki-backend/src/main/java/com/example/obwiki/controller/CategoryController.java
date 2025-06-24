package com.example.obwiki.controller;

import com.example.obwiki.rep.CategoryQueryReq;
import com.example.obwiki.rep.CategorySaveReq;
import com.example.obwiki.resp.CategoryQueryResp;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.resp.PageResp;
import com.example.obwiki.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;


    @GetMapping("/list")
    public CommonResp list(@Valid CategoryQueryReq req) {
        CommonResp<PageResp<CategoryQueryResp>> resp = new CommonResp<>(true,"查询成功",null);

        PageResp<CategoryQueryResp> pageResp = categoryService.listByname(req);
        resp.setContent(pageResp);

        return resp;
    }

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody CategorySaveReq req) {
        CommonResp resp = new CommonResp<>(true,"成功",null);
        categoryService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>(true,"删除成功",null);
        categoryService.delete(id);
        return resp;
    }
}
