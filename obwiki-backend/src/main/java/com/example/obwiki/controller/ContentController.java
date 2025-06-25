package com.example.obwiki.controller;

import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.service.IContentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
@RestController
@RequestMapping("/obwiki/content")
@Api(value = "内容接口", tags = {"内容相关接口"})
public class ContentController {
    @Autowired
    private IContentService contentService;

    @ApiOperation(value = "根据文档ID查询内容", notes = "通过文档ID获取对应的内容详情")
    @ApiImplicitParam(name = "id", value = "文档ID", required = true, dataType = "long", paramType = "path")
    @GetMapping("/findContent/{id}")
    public CommonResp<String> findContent(@PathVariable Long id) {
        CommonResp<String> resp = new CommonResp<>();
        String content = contentService.findContent(id);
        resp.setContent(content);
        return resp;
    }
}
