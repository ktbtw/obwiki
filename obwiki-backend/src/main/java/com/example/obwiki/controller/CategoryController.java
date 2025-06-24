package com.example.obwiki.controller;

import com.example.obwiki.rep.CategoryQueryReq;
import com.example.obwiki.rep.CategorySaveReq;
import com.example.obwiki.resp.CategoryQueryResp;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.resp.PageResp;
import com.example.obwiki.service.ICategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

@Api(tags = "分类管理接口")
@RestController
@RequestMapping("/obwiki/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @ApiOperation("分页查询分类信息")
    @GetMapping("/list")
    public CommonResp list(@Valid CategoryQueryReq req) {
        CommonResp<PageResp<CategoryQueryResp>> resp = new CommonResp<>(true,"查询成功",null);

        PageResp<CategoryQueryResp> pageResp = categoryService.listByname(req);
        resp.setContent(pageResp);

        return resp;
    }

    @ApiOperation("查询全部分类信息")
    @GetMapping("/all")
    public CommonResp all() {
        CommonResp<List<CategoryQueryResp>> resp = new CommonResp<>(true,"查询成功",null);

        List<CategoryQueryResp> list = categoryService.all();
        resp.setContent(list);
        return resp;
    }

    @ApiOperation("保存分类信息（新增或更新）")
    @PostMapping("/save")
    public CommonResp save(@ApiParam("分类保存请求体") @Valid @RequestBody CategorySaveReq req) {
        CommonResp resp = new CommonResp<>(true,"成功",null);
        categoryService.save(req);
        return resp;
    }

    @ApiOperation("根据 ID 删除分类")
    @ApiImplicitParam(name = "id", value = "分类ID", required = true, dataType = "long", paramType = "path")
    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>(true,"删除成功",null);
        categoryService.delete(id);
        return resp;
    }
}
